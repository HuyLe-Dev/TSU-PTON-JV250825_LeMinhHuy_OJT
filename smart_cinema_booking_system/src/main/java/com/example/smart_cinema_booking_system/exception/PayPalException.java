package com.example.smart_cinema_booking_system.exception;

/**
 * Exception riêng cho lỗi liên quan đến PayPal API.
 * Giúp phân biệt lỗi PayPal với lỗi business logic khác.
 */
public class PayPalException extends RuntimeException {

    private final int httpStatus;
    private final String paypalErrorDetail;

    public PayPalException(String message) {
        super(message);
        this.httpStatus = 0;
        this.paypalErrorDetail = null;
    }

    public PayPalException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = 0;
        this.paypalErrorDetail = null;
    }

    public PayPalException(String message, int httpStatus, String paypalErrorDetail) {
        super(message);
        this.httpStatus = httpStatus;
        this.paypalErrorDetail = paypalErrorDetail;
    }

    public PayPalException(String message, int httpStatus, String paypalErrorDetail, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.paypalErrorDetail = paypalErrorDetail;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getPaypalErrorDetail() {
        return paypalErrorDetail;
    }
}
