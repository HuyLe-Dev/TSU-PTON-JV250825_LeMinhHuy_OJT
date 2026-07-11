package com.example.smart_cinema_booking_system.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.smart_cinema_booking_system.enums.BookingStatus;
import com.example.smart_cinema_booking_system.exception.BusinessException;
import com.example.smart_cinema_booking_system.service.BookingService;
import com.example.smart_cinema_booking_system.service.StatsService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller dành cho Admin quản lý Đơn đặt vé và Doanh thu.
 */
@Controller
@RequestMapping("/admin/bookings")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminBookingController {

    private final BookingService bookingService;
    private final StatsService statsService;

    @GetMapping
    public String manageBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {
        org.springframework.data.domain.Page<com.example.smart_cinema_booking_system.dto.response.BookingHistoryDTO> bookingPage = bookingService
                .getAllBookingsPaged(page, size);

        model.addAttribute("bookings", bookingPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookingPage.getTotalPages());
        model.addAttribute("totalItems", bookingPage.getTotalElements());
        model.addAttribute("ticketsPaidCount", statsService.getTicketsCountForCurrentMonth(BookingStatus.PAID));
        model.addAttribute("ticketsCancelledCount",
                statsService.getTicketsCountForCurrentMonth(BookingStatus.CANCELLED));
        return "admin/bookings";
    }

    @PostMapping("/{id}/confirm")
    public String confirmPayment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.confirmPayment(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xác nhận thanh toán thành công!");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/bookings";
    }

    @PostMapping("/{id}/cancel")
    public String cancelBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBookingAdmin(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn đặt vé thành công!");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/bookings";
    }
}
