package com.example.smart_cinema_booking_system.controller;

import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.smart_cinema_booking_system.enums.BookingStatus;
import com.example.smart_cinema_booking_system.exception.BusinessException;
import com.example.smart_cinema_booking_system.service.BookingService;
import com.example.smart_cinema_booking_system.service.EmailService;
import com.example.smart_cinema_booking_system.service.StatsService;

import lombok.RequiredArgsConstructor;

/**
 * Controller dành riêng cho Nhân viên rạp (STAFF).
 * Chức năng: Tra cứu mã đơn hàng, xác nhận thanh toán, hủy đơn.
 */
@Controller
@RequestMapping("/staff")
@PreAuthorize("hasRole('STAFF')")
@RequiredArgsConstructor
public class StaffController {

    private final BookingService bookingService;
    private final StatsService statsService;
    private final EmailService emailService;

    @GetMapping("/dashboard")
    public String dashboard() {
        return "redirect:/staff/bookings";
    }

    @GetMapping("/bookings")
    public String manageBookings(
            @RequestParam(required = false) Long searchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        if (searchId != null) {
            try {
                com.example.smart_cinema_booking_system.entity.Booking b = bookingService.getBookingById(searchId);
                com.example.smart_cinema_booking_system.dto.response.BookingHistoryDTO dto = new com.example.smart_cinema_booking_system.dto.response.BookingHistoryDTO();
                dto.setBookingId(b.getBookingId());
                dto.setMovieTitle(b.getShowtime().getMovie().getTitle());
                dto.setPosterUrl(b.getShowtime().getMovie().getPosterUrl());
                dto.setRoomName(b.getShowtime().getRoom().getRoomName());
                dto.setShowtimeStart(b.getShowtime().getStartTime());
                dto.setBookingDate(b.getBookingDate());
                dto.setTotalAmount(b.getTotalAmount());
                dto.setPaymentMethod(b.getPaymentMethod());
                dto.setStatus(b.getBookingStatus());
                dto.setUsername(b.getUser().getUsername());

                String seats = b.getBookedSeatNames();
                if (seats == null || seats.isEmpty()) {
                    seats = b.getTickets().stream()
                            .map(t -> t.getSeat().getSeatName())
                            .collect(Collectors.joining(", "));
                }
                dto.setSeats(seats);
                dto.setCancellable(b.getBookingStatus() != BookingStatus.CANCELLED);

                model.addAttribute("bookings", java.util.Collections.singletonList(dto));
                model.addAttribute("currentPage", 0);
                model.addAttribute("totalPages", 1);
                model.addAttribute("totalItems", 1);
            } catch (BusinessException ex) {
                model.addAttribute("errorMessage", "Không tìm thấy mã đơn hàng: " + searchId);
                model.addAttribute("bookings", java.util.Collections.emptyList());
                model.addAttribute("currentPage", 0);
                model.addAttribute("totalPages", 0);
                model.addAttribute("totalItems", 0);
            }
        } else {
            org.springframework.data.domain.Page<com.example.smart_cinema_booking_system.dto.response.BookingHistoryDTO> bookingPage = bookingService
                    .getAllBookingsPaged(page, size);

            model.addAttribute("bookings", bookingPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", bookingPage.getTotalPages());
            model.addAttribute("totalItems", bookingPage.getTotalElements());
        }

        model.addAttribute("ticketsPaidCount", statsService.getTicketsCountForCurrentMonth(BookingStatus.PAID));
        model.addAttribute("ticketsCancelledCount",
                statsService.getTicketsCountForCurrentMonth(BookingStatus.CANCELLED));
        return "staff/bookings";
    }

    @PostMapping("/bookings/{id}/confirm")
    public String confirmPayment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.confirmPayment(id);
            // Async send ticket email
            emailService.sendTicketEmail(id);

            redirectAttributes.addFlashAttribute("successMessage", "Đã xác nhận thanh toán thành công!");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/staff/bookings";
    }

    @PostMapping("/bookings/{id}/cancel")
    public String cancelBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBookingAdmin(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn đặt vé thành công!");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/staff/bookings";
    }
}
