package com.example.smart_cinema_booking_system.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.smart_cinema_booking_system.exception.BusinessException;
import com.example.smart_cinema_booking_system.repository.MovieRepository;
import com.example.smart_cinema_booking_system.repository.ShowtimeRepository;
import com.example.smart_cinema_booking_system.dto.response.MovieResponseDTO;
import com.example.smart_cinema_booking_system.enums.MovieStatus;
import com.example.smart_cinema_booking_system.service.MovieService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final MovieService movieService;
    private final MovieRepository movieRepository;
    private final ShowtimeRepository showtimeRepository;

    @GetMapping("/")
    public String home(Model model) {
        List<MovieResponseDTO> allMovies = movieService.getAllMovies();

        List<MovieResponseDTO> nowShowing = allMovies.stream()
                .filter(m -> m.getStatus() == MovieStatus.NOW_SHOWING)
                .toList();

        List<MovieResponseDTO> comingSoon = allMovies.stream()
                .filter(m -> m.getStatus() == MovieStatus.COMING_SOON)
                .toList();

        model.addAttribute("nowShowing", nowShowing);
        model.addAttribute("comingSoon", comingSoon);

        // Featured movie: first NOW_SHOWING, fallback to first of all
        if (!nowShowing.isEmpty()) {
            model.addAttribute("featured", nowShowing.get(0));
        } else if (!allMovies.isEmpty()) {
            model.addAttribute("featured", allMovies.get(0));
        }

        return "index";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/access-denied";
    }

    @GetMapping("/movies/{id}")
    public String movieDetail(@PathVariable Long id, Model model) {
        var movie = movieRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy phim!"));
        model.addAttribute("movie", movie);
        model.addAttribute("showtimes", showtimeRepository.findAll().stream().filter(s -> s.getMovie().getMovieId().equals(id)).toList());
        return "movie-detail";
    }
}
