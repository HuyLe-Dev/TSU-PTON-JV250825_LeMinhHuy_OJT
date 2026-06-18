package com.example.smart_cinema_booking_system.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Xử lý khi user ĐÃ đăng nhập nhưng KHÔNG có quyền truy cập tài nguyên.
 * Ví dụ: USER cố vào /admin/** hoặc STAFF cố vào /admin/**
 * → Redirect về trang "Không có quyền truy cập" thay vì hiển thị lỗi 403 mặc định.
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.sendRedirect("/access-denied");
    }
}
