package com.example.smart_cinema_booking_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeatInfoDTO {
    private Long seatId;
    private String seatName;
    private String seatType;
    private Boolean isBooked;
}
