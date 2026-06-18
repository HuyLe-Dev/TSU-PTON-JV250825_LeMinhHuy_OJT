package com.example.smart_cinema_booking_system.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller dành cho Khách hàng (USER) đã đăng nhập.
 * Khu vực cá nhân: xem hồ sơ, lịch sử đặt vé, hủy vé.
 */
@Controller
@RequestMapping("/user")
@PreAuthorize("hasRole('USER')")
public class UserController {

    @GetMapping("/profile")
    public String profile() {
        return "user/profile";
    }

    @GetMapping("/bookings")
    public String bookingHistory() {
        return "user/bookings";
    }
}
