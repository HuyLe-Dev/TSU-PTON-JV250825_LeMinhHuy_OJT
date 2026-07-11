package com.example.smart_cinema_booking_system.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.smart_cinema_booking_system.enums.BookingStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingHistoryDTO {
    private Long bookingId;
    private String movieTitle;
    private String posterUrl;
    private String roomName;
    private LocalDateTime showtimeStart;
    private LocalDateTime bookingDate;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private BookingStatus status;
    private String seats;
    private boolean cancellable;
    private String username;
}
