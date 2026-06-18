package com.example.smart_cinema_booking_system.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Xử lý khi user chưa xác thực (token hết hạn hoặc chưa login) cố truy cập tài nguyên bảo vệ.
 * Redirect về trang login thay vì trả về lỗi 401/403 mặc định.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        // Redirect về trang login khi chưa xác thực
        response.sendRedirect("/auth/login");
    }
}
