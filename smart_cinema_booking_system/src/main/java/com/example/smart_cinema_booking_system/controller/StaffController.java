package com.example.smart_cinema_booking_system.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller dành riêng cho Nhân viên rạp (STAFF).
 * Giao diện Quản lý Đơn hàng đã được gộp chung với Admin tại /admin/bookings.
 * Controller này chỉ làm nhiệm vụ chuyển hướng (Redirect) nếu Staff lỡ tay gõ URL cũ.
 */
@Controller
@RequestMapping("/staff")
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
public class StaffController {

    @GetMapping({"/dashboard", "/bookings"})
    public String redirectDashboard() {
        return "redirect:/admin/bookings";
    }
}
