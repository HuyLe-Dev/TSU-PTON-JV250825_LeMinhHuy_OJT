package com.example.smart_cinema_booking_system.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller dành riêng cho Nhân viên rạp (STAFF).
 * Tất cả endpoint /staff/** đã được bảo vệ bởi SecurityConfig (hasRole('STAFF')).
 * Nhân viên có thể: tra cứu mã đơn hàng, xác nhận thanh toán, in vé.
 */
@Controller
@RequestMapping("/staff")
@PreAuthorize("hasRole('STAFF')")
public class StaffController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "staff/dashboard";
    }

    @GetMapping("/bookings")
    public String manageBookings() {
        return "staff/bookings";
    }
}
