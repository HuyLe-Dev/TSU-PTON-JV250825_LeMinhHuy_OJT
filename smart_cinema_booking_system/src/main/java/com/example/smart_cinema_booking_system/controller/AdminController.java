package com.example.smart_cinema_booking_system.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller dành riêng cho Admin.
 * Tất cả endpoint /admin/** đã được bảo vệ bởi SecurityConfig (hasRole('ADMIN')).
 * Thêm @PreAuthorize ở method level để phòng ngừa lớp thứ 2.
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/movies")
    public String manageMovies() {
        return "admin/movies";
    }

    @GetMapping("/showtimes")
    public String manageShowtimes() {
        return "admin/showtimes";
    }
}
