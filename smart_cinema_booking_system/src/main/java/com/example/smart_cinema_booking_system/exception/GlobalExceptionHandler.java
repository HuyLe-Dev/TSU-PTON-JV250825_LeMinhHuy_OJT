package com.example.smart_cinema_booking_system.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Xử lý lỗi tập trung cho toàn bộ ứng dụng.
 * Thay vì try-catch ở từng Controller, các exception sẽ được bắt tại đây.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Xử lý các lỗi nghiệp vụ (BusinessException).
     * VD: Đặt ghế đã có người, hủy vé quá 24h, phim không tồn tại.
     */
    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(BusinessException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/business-error";
    }

    /**
     * KHÔNG bắt 404 — để Spring Boot tự render templates/error/404.html
     */
    @ExceptionHandler({ NoHandlerFoundException.class, NoResourceFoundException.class })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound() {
        return "error/404";
    }

    /**
     * KHÔNG bắt AccessDeniedException — để CustomAccessDeniedHandler xử lý.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied() {
        return "error/access-denied";
    }

    /**
     * Xử lý các lỗi không mong muốn (Exception chung = 500).
     * Chỉ bắt những gì không thuộc các case trên.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception ex, Model model) {
        model.addAttribute("errorMessage", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.");
        return "error/generic-error";
    }
}
