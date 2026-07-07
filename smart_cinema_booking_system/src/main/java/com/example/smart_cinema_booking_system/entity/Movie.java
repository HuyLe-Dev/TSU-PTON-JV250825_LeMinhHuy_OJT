package com.example.smart_cinema_booking_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

import com.example.smart_cinema_booking_system.enums.MovieStatus;

@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Movie extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movie_id")
    private Long movieId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer duration;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(length = 50)
    private String language;

    @Column(name = "poster_url")
    private String posterUrl;

    @Column(name = "backdrop_url")
    private String backdropUrl;

    @Column(name = "trailer_url")
    private String trailerUrl;

    @Column(name = "age_rating", length = 20)
    private String ageRating;

    @Enumerated(EnumType.STRING)
    private MovieStatus status;

    // Quan hệ N-N tự động tạo bảng trung gian "movie_genres"
    @ManyToMany
    @JoinTable(
        name = "movie_genres",
        joinColumns = @JoinColumn(name = "movie_id"),
        inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private List<Genre> genres;
}