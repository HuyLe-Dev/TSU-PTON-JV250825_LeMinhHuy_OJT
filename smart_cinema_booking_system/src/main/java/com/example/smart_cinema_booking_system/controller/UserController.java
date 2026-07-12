package com.example.smart_cinema_booking_system.controller;

import java.security.Principal;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.smart_cinema_booking_system.exception.BusinessException;
import com.example.smart_cinema_booking_system.service.BookingService;

import lombok.RequiredArgsConstructor;

/**
 * Controller dành cho Khách hàng (USER) đã đăng nhập.
 * Khu vực cá nhân: xem hồ sơ, lịch sử đặt vé, hủy vé.
 */
@Controller
@RequestMapping("/user")
@PreAuthorize("hasAnyRole('USER', 'STAFF', 'ADMIN')")
@RequiredArgsConstructor
public class UserController {

    private final BookingService bookingService;

    @GetMapping("/profile")
    public String profile() {
        return "user/profile";
    }

    @GetMapping("/bookings")
    public String bookingHistory(Principal principal, Model model) {
        model.addAttribute("bookings", bookingService.getBookingHistory(principal.getName()));
        return "user/bookings";
    }

    @PostMapping("/bookings/{bookingId}/cancel")
    public String cancelBooking(@PathVariable Long bookingId,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBooking(bookingId, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Hủy vé thành công! Ghế đã được giải phóng.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/user/bookings";
    }
}
