package com.example.smart_cinema_booking_system.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

/**
 * PayPal configuration bean.
 * Đọc credentials từ application.properties và cung cấp RestTemplate riêng cho PayPal API calls.
 */
@Configuration
@Getter
public class PayPalConfig {

    @Value("${paypal.client-id}")
    private String clientId;

    @Value("${paypal.client-secret}")
    private String clientSecret;

    @Value("${paypal.mode}")
    private String mode;

    @Value("${paypal.base-url}")
    private String baseUrl;

    @Value("${paypal.currency}")
    private String currency;

    @Value("${paypal.vnd-to-usd-rate}")
    private double vndToUsdRate;

    @Value("${paypal.payment-timeout-minutes}")
    private int paymentTimeoutMinutes;
}
