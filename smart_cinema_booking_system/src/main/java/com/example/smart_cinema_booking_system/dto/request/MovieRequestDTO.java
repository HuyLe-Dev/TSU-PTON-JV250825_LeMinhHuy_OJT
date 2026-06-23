package com.example.smart_cinema_booking_system.dto.request;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.smart_cinema_booking_system.enums.MovieStatus;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MovieRequestDTO {

    @NotBlank(message = "Tên phim không được để trống")
    private String title;

    private String description;

    @NotNull(message = "Thời lượng không được để trống")
    @Min(value = 1, message = "Thời lượng phải lớn hơn 0")
    private Integer duration;

    @NotNull(message = "Ngày phát hành không được để trống")
    private LocalDate releaseDate;

    private String language;

    private MultipartFile posterFile;

    private String trailerUrl;

    @NotBlank(message = "Giới hạn độ tuổi không được để trống")
    private String ageRating;

    @NotNull(message = "Trạng thái không được để trống")
    private MovieStatus status;

    @NotNull(message = "Vui lòng chọn ít nhất một thể loại")
    private List<Long> genreIds;
}
