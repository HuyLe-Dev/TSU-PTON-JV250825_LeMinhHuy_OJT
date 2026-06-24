package com.example.smart_cinema_booking_system.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.smart_cinema_booking_system.enums.ShowtimeStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShowtimeResponseDTO {
    private Long showtimeId;
    private String movieTitle;
    private String moviePosterUrl;
    private String roomName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal ticketPrice;
    private ShowtimeStatus status;
}
