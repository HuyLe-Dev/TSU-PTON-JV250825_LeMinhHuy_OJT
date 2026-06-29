package com.example.smart_cinema_booking_system.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingRequestDTO {

    @NotNull(message = "Thiếu thông tin suất chiếu")
    private Long showtimeId;

    @NotEmpty(message = "Vui lòng chọn ít nhất 1 ghế")
    private List<Long> seatIds;

    @NotNull(message = "Vui lòng chọn phương thức thanh toán")
    private String paymentMethod;
}
