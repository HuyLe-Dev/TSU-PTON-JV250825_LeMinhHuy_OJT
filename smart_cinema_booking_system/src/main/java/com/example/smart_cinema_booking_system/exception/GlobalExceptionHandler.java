package com.example.smart_cinema_booking_system.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Xử lý lỗi tập trung cho toàn bộ ứng dụng.
 * Thay vì try-catch ở từng Controller, các exception sẽ được bắt tại đây.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Xử lý các lỗi nghiệp vụ (BusinessException).
     * Trả về trang lỗi chung với thông báo cụ thể.
     */
    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(BusinessException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/business-error";
    }

    /**
     * Xử lý các lỗi không mong muốn (Exception chung).
     */
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model) {
        model.addAttribute("errorMessage", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.");
        return "error/generic-error";
    }
}
