package com.example.smart_cinema_booking_system.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.example.smart_cinema_booking_system.config.PayPalConfig;
import com.example.smart_cinema_booking_system.exception.PayPalException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service gọi PayPal REST API v2.
 * - getAccessToken(): lấy OAuth2 Bearer token
 * - createOrder(): tạo order trên PayPal, trả về approve URL
 * - captureOrder(): capture payment sau khi user approve
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PayPalService {

    private final PayPalConfig payPalConfig;

    /**
     * Lấy OAuth2 Access Token từ PayPal API.
     * Endpoint: POST {baseUrl}/v1/oauth2/token
     * Auth: Basic (client-id:client-secret)
     * Body: grant_type=client_credentials
     *
     * @return access_token string
     * @throws PayPalException nếu không lấy được token
     */
    public String getAccessToken() {
        String url = payPalConfig.getBaseUrl() + "/v1/oauth2/token";

        String credentials = payPalConfig.getClientId() + ":" + payPalConfig.getClientSecret();
        String encodedCredentials = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedCredentials);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> request = new HttpEntity<>("grant_type=client_credentials", headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String accessToken = jsonNode.get("access_token").asText();

            log.debug("PayPal access token obtained. Token type: {}", jsonNode.get("token_type").asText());
            return accessToken;

        } catch (HttpClientErrorException e) {
            // 4xx: credentials sai, unauthorized
            log.error("PayPal auth failed (HTTP {}): {}", e.getStatusCode().value(), e.getResponseBodyAsString());
            throw new PayPalException("Xác thực PayPal thất bại. Vui lòng kiểm tra cấu hình.",
                    e.getStatusCode().value(), e.getResponseBodyAsString(), e);
        } catch (HttpServerErrorException e) {
            // 5xx: PayPal server error
            log.error("PayPal server error (HTTP {}): {}", e.getStatusCode().value(), e.getResponseBodyAsString());
            throw new PayPalException("PayPal đang gặp sự cố. Vui lòng thử lại sau.",
                    e.getStatusCode().value(), e.getResponseBodyAsString(), e);
        } catch (ResourceAccessException e) {
            // Timeout / connection refused
            log.error("PayPal connection failed: {}", e.getMessage());
            throw new PayPalException("Không thể kết nối đến PayPal. Kiểm tra kết nối mạng.", e);
        } catch (Exception e) {
            log.error("Unexpected error obtaining PayPal access token: {}", e.getMessage(), e);
            throw new PayPalException("Lỗi không xác định khi kết nối PayPal.", e);
        }
    }

    /**
     * Tạo PayPal Order (Orders API v2).
     * Convert tổng tiền VND sang USD, gửi request tạo order,
     * trả về approve URL để redirect user sang PayPal.
     *
     * @param totalAmountVND tổng tiền booking (VND)
     * @param bookingId      ID booking trong hệ thống (dùng làm reference)
     * @param returnUrl      URL callback khi user approve thanh toán
     * @param cancelUrl      URL callback khi user hủy trên PayPal
     * @return approve URL (redirect user sang đây)
     * @throws PayPalException nếu tạo order thất bại
     */
    public String createOrder(BigDecimal totalAmountVND, Long bookingId, String returnUrl, String cancelUrl) {
        String accessToken = getAccessToken();
        String url = payPalConfig.getBaseUrl() + "/v2/checkout/orders";

        // Convert VND → USD, làm tròn 2 chữ số
        BigDecimal amountUSD = totalAmountVND.divide(
                BigDecimal.valueOf(payPalConfig.getVndToUsdRate()), 2, RoundingMode.HALF_UP);

        log.info("Creating PayPal order. BookingId: {}, AmountVND: {}, AmountUSD: {}", bookingId, totalAmountVND, amountUSD);

        // Build request body theo PayPal Orders API v2 format
        Map<String, Object> orderRequest = Map.of(
                "intent", "CAPTURE",
                "purchase_units", List.of(
                        Map.of(
                                "reference_id", "BOOKING_" + bookingId,
                                "description", "Smart Cinema - Booking #" + bookingId,
                                "amount", Map.of(
                                        "currency_code", payPalConfig.getCurrency(),
                                        "value", amountUSD.toString()
                                )
                        )
                ),
                "application_context", Map.of(
                        "brand_name", "Smart Cinema",
                        "landing_page", "LOGIN",
                        "user_action", "PAY_NOW",
                        "return_url", returnUrl,
                        "cancel_url", cancelUrl
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String requestBody = objectMapper.writeValueAsString(orderRequest);
            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String orderId = jsonNode.get("id").asText();

            // Tìm approve link trong response
            String approveUrl = null;
            for (JsonNode link : jsonNode.get("links")) {
                if ("approve".equals(link.get("rel").asText())) {
                    approveUrl = link.get("href").asText();
                    break;
                }
            }

            if (approveUrl == null) {
                log.error("PayPal response missing approve URL. Response: {}", response.getBody());
                throw new PayPalException("PayPal không trả về đường dẫn thanh toán.");
            }

            log.info("PayPal order created successfully. OrderId: {}, BookingId: {}, Amount: {} USD",
                    orderId, bookingId, amountUSD);

            return approveUrl;

        } catch (PayPalException e) {
            throw e;
        } catch (HttpClientErrorException e) {
            log.error("PayPal create order failed (HTTP {}). BookingId: {}. Response: {}",
                    e.getStatusCode().value(), bookingId, e.getResponseBodyAsString());
            throw new PayPalException("Không thể tạo đơn thanh toán PayPal.",
                    e.getStatusCode().value(), e.getResponseBodyAsString(), e);
        } catch (HttpServerErrorException e) {
            log.error("PayPal server error during create order (HTTP {}). BookingId: {}",
                    e.getStatusCode().value(), bookingId);
            throw new PayPalException("PayPal đang gặp sự cố. Vui lòng thử lại sau.",
                    e.getStatusCode().value(), e.getResponseBodyAsString(), e);
        } catch (ResourceAccessException e) {
            log.error("PayPal connection timeout during create order. BookingId: {}", bookingId);
            throw new PayPalException("Không thể kết nối đến PayPal. Kiểm tra kết nối mạng.", e);
        } catch (Exception e) {
            log.error("Unexpected error creating PayPal order. BookingId: {}: {}", bookingId, e.getMessage(), e);
            throw new PayPalException("Lỗi không xác định khi tạo đơn PayPal.", e);
        }
    }

    /**
     * Capture PayPal Order sau khi user đã approve trên PayPal.
     * Endpoint: POST {baseUrl}/v2/checkout/orders/{orderId}/capture
     *
     * @param orderId PayPal Order ID (nhận từ callback URL parameter "token")
     * @return PayPal Order ID nếu capture thành công (status = COMPLETED)
     * @throws PayPalException nếu capture thất bại hoặc status không phải COMPLETED
     */
    public String captureOrder(String orderId) {
        log.info("Capturing PayPal order. OrderId: {}", orderId);

        String accessToken = getAccessToken();
        String url = payPalConfig.getBaseUrl() + "/v2/checkout/orders/" + orderId + "/capture";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>("", headers);
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String status = jsonNode.get("status").asText();

            if (!"COMPLETED".equals(status)) {
                log.warn("PayPal capture non-COMPLETED. OrderId: {}, Status: {}, Response: {}",
                        orderId, status, response.getBody());
                throw new PayPalException("Thanh toán PayPal chưa hoàn tất. Trạng thái: " + status);
            }

            log.info("PayPal payment captured successfully. OrderId: {}, Status: COMPLETED", orderId);
            return orderId;

        } catch (PayPalException e) {
            throw e;
        } catch (HttpClientErrorException e) {
            log.error("PayPal capture failed (HTTP {}). OrderId: {}. Response: {}",
                    e.getStatusCode().value(), orderId, e.getResponseBodyAsString());

            // 422 UNPROCESSABLE_ENTITY thường là order đã capture hoặc expired
            if (e.getStatusCode().value() == 422) {
                throw new PayPalException("Đơn thanh toán PayPal đã được xử lý hoặc hết hạn.",
                        422, e.getResponseBodyAsString(), e);
            }
            throw new PayPalException("Xác nhận thanh toán PayPal thất bại.",
                    e.getStatusCode().value(), e.getResponseBodyAsString(), e);
        } catch (HttpServerErrorException e) {
            log.error("PayPal server error during capture (HTTP {}). OrderId: {}", e.getStatusCode().value(), orderId);
            throw new PayPalException("PayPal đang gặp sự cố. Vui lòng thử lại sau.",
                    e.getStatusCode().value(), e.getResponseBodyAsString(), e);
        } catch (ResourceAccessException e) {
            log.error("PayPal connection timeout during capture. OrderId: {}", orderId);
            throw new PayPalException("Không thể kết nối đến PayPal để xác nhận thanh toán.", e);
        } catch (Exception e) {
            log.error("Unexpected error capturing PayPal order {}. Error: {}", orderId, e.getMessage(), e);
            throw new PayPalException("Lỗi không xác định khi xác nhận thanh toán PayPal.", e);
        }
    }
}
