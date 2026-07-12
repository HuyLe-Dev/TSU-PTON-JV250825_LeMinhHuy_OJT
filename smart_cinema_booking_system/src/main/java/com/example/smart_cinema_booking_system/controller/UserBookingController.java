package com.example.smart_cinema_booking_system.controller;

import java.security.Principal;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.smart_cinema_booking_system.dto.request.BookingRequestDTO;
import com.example.smart_cinema_booking_system.entity.Booking;
import com.example.smart_cinema_booking_system.exception.BusinessException;
import com.example.smart_cinema_booking_system.repository.ShowtimeRepository;
import com.example.smart_cinema_booking_system.service.BookingService;
import com.example.smart_cinema_booking_system.service.EmailService;
import com.example.smart_cinema_booking_system.service.PayPalService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/user/book")
@PreAuthorize("hasAnyRole('USER', 'STAFF', 'ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class UserBookingController {

    private final BookingService bookingService;
    private final ShowtimeRepository showtimeRepository;
    private final PayPalService payPalService;
    private final EmailService emailService;

    @GetMapping("/{showtimeId}")
    public String showSeatSelection(@PathVariable Long showtimeId, Model model) {
        var showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy suất chiếu!"));

        model.addAttribute("showtime", showtime);
        model.addAttribute("room", showtime.getRoom());
        model.addAttribute("seatsInfo", bookingService.getSeatMap(showtimeId));

        BookingRequestDTO dto = new BookingRequestDTO();
        dto.setShowtimeId(showtimeId);
        model.addAttribute("bookingDTO", dto);

        return "user/seat-selection";
    }

    @PostMapping("/submit")
    public String submitBooking(@Valid @ModelAttribute("bookingDTO") BookingRequestDTO dto,
                                BindingResult result,
                                Principal principal,
                                HttpServletRequest httpRequest,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", result.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/user/book/" + dto.getShowtimeId();
        }

        try {
            // Tạo booking (status = PENDING) cho mọi payment method
            Long bookingId = bookingService.createBooking(principal.getName(), dto);

            // Nếu chọn PayPal → tạo PayPal Order và redirect sang PayPal
            if ("PAYPAL".equalsIgnoreCase(dto.getPaymentMethod())) {
                return handlePayPalPayment(bookingId, httpRequest, redirectAttributes, dto.getShowtimeId());
            }

            // Flow mặc định (CASH, VNPAY, MOMO) — giữ nguyên như cũ
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đặt vé thành công! Bạn có thể xem chi tiết trong Lịch sử mua vé.");
            return "redirect:/";

        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Rất tiếc! Ghế bạn chọn đã có người nhanh tay đặt trước. Vui lòng chọn ghế khác.");
            return "redirect:/user/book/" + dto.getShowtimeId();
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/user/book/" + dto.getShowtimeId();
        } catch (Exception e) {
            log.error("Error during booking submission: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Có lỗi hệ thống xảy ra, vui lòng thử lại sau.");
            return "redirect:/user/book/" + dto.getShowtimeId();
        }
    }

    /**
     * Xử lý flow PayPal: tạo PayPal Order → redirect user sang PayPal approve.
     */
    private String handlePayPalPayment(Long bookingId, HttpServletRequest httpRequest,
                                       RedirectAttributes redirectAttributes, Long showtimeId) {
        // Build callback URLs dựa trên request hiện tại
        String baseUrl = getBaseUrl(httpRequest);
        String returnUrl = baseUrl + "/user/book/paypal/success";
        String cancelUrl = baseUrl + "/user/book/paypal/cancel";

        try {
            // Lấy totalAmount từ booking vừa tạo
            var bookingEntity = bookingService.getBookingById(bookingId);
            var totalAmount = bookingEntity.getTotalAmount();

            // Tạo PayPal Order
            String approveUrl = payPalService.createOrder(totalAmount, bookingId, returnUrl, cancelUrl);

            // Lưu paypalOrderId vào booking (extract từ approve URL)
            String paypalOrderId = extractOrderIdFromUrl(approveUrl);
            bookingService.updatePaypalOrderId(bookingId, paypalOrderId);

            log.info("Redirecting to PayPal for bookingId: {}, paypalOrderId: {}", bookingId, paypalOrderId);

            // Redirect user sang PayPal
            return "redirect:" + approveUrl;

        } catch (Exception e) {
            log.error("PayPal payment initiation failed for bookingId {}: {}", bookingId, e.getMessage(), e);
            // PayPalException đã carry message tiếng Việt, dùng trực tiếp
            String errorMsg = e.getMessage() != null ? e.getMessage()
                    : "Không thể kết nối đến PayPal. Vui lòng thử lại hoặc chọn phương thức khác.";
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            return "redirect:/user/book/" + showtimeId;
        }
    }

    /**
     * PayPal callback: user approve thanh toán thành công.
     * PayPal redirect về đây với param "token" = PayPal Order ID.
     */
    @GetMapping("/paypal/success")
    public String paypalSuccess(@RequestParam("token") String paypalOrderId,
                                RedirectAttributes redirectAttributes) {
        try {
            // Tìm booking theo paypalOrderId
            Booking booking = bookingService.findByPaypalOrderId(paypalOrderId);

            // Kiểm tra nếu vé đã được thanh toán (tránh lỗi khi user F5 lại trang)
            if (booking.getBookingStatus() == com.example.smart_cinema_booking_system.enums.BookingStatus.PAID) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Thanh toán PayPal thành công! Vé đã được xác nhận.");
                return "redirect:/";
            }

            // Capture payment trên PayPal
            payPalService.captureOrder(paypalOrderId);

            // Cập nhật status → PAID
            bookingService.confirmPayment(booking.getBookingId());
            
            // Async send ticket email
            emailService.sendTicketEmail(booking.getBookingId());

            log.info("PayPal payment successful. BookingId: {}, PayPalOrderId: {}",
                    booking.getBookingId(), paypalOrderId);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Thanh toán PayPal thành công! Vé đã được xác nhận.");
            return "redirect:/";

        } catch (Exception e) {
            log.error("PayPal capture failed for orderId {}: {}", paypalOrderId, e.getMessage(), e);
            String errorMsg = e.getMessage() != null ? e.getMessage()
                    : "Xác nhận thanh toán PayPal thất bại. Vui lòng liên hệ quầy hỗ trợ.";
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            return "redirect:/";
        }
    }

    /**
     * PayPal callback: user hủy thanh toán trên PayPal.
     * Booking giữ status PENDING (sẽ bị auto-cancel sau 15 phút bởi scheduler).
     */
    @GetMapping("/paypal/cancel")
    public String paypalCancel(@RequestParam(value = "token", required = false) String paypalOrderId,
                               RedirectAttributes redirectAttributes) {
        log.info("User cancelled PayPal payment. PayPalOrderId: {}", paypalOrderId);
        redirectAttributes.addFlashAttribute("errorMessage",
                "Bạn đã hủy thanh toán PayPal. Đơn đặt vé sẽ tự động hủy sau 15 phút nếu không thanh toán.");
        return "redirect:/";
    }

    /**
     * Lấy base URL từ HttpServletRequest (scheme + host + port).
     */
    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        if ((scheme.equals("http") && serverPort == 80) ||
            (scheme.equals("https") && serverPort == 443)) {
            return scheme + "://" + serverName;
        }
        return scheme + "://" + serverName + ":" + serverPort;
    }

    /**
     * Extract PayPal Order ID từ approve URL.
     * URL format: https://www.sandbox.paypal.com/checkoutnow?token=ORDER_ID
     */
    private String extractOrderIdFromUrl(String approveUrl) {
        String tokenParam = "token=";
        int index = approveUrl.indexOf(tokenParam);
        if (index == -1) {
            throw new RuntimeException("Cannot extract order ID from PayPal approve URL: " + approveUrl);
        }
        String orderId = approveUrl.substring(index + tokenParam.length());
        // Remove any trailing params
        int ampIndex = orderId.indexOf('&');
        if (ampIndex != -1) {
            orderId = orderId.substring(0, ampIndex);
        }
        return orderId;
    }
}
