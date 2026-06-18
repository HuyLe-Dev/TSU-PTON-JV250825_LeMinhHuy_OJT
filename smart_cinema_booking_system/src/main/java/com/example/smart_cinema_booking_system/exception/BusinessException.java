package com.example.smart_cinema_booking_system.exception;

/**
 * Exception tùy chỉnh cho các lỗi nghiệp vụ (business logic).
 * Sử dụng thay cho IllegalArgumentException để dễ xử lý tập trung.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
