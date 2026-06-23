package com.example.smart_cinema_booking_system.dto.response;

import java.time.LocalDate;
import java.util.List;

import com.example.smart_cinema_booking_system.entity.Genre;
import com.example.smart_cinema_booking_system.enums.MovieStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MovieResponseDTO {
    private Long movieId;
    private String title;
    private String description;
    private Integer duration;
    private LocalDate releaseDate;
    private String language;
    private String posterUrl;
    private String trailerUrl;
    private String ageRating;
    private MovieStatus status;
    private List<Genre> genres;
}
