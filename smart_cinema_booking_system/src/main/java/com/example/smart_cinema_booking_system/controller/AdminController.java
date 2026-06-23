package com.example.smart_cinema_booking_system.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.smart_cinema_booking_system.dto.request.MovieRequestDTO;
import com.example.smart_cinema_booking_system.enums.MovieStatus;
import com.example.smart_cinema_booking_system.repository.GenreRepository;
import com.example.smart_cinema_booking_system.service.MovieService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller dành riêng cho Admin.
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final MovieService movieService;
    private final GenreRepository genreRepository;

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/movies")
    public String manageMovies(Model model) {
        model.addAttribute("movies", movieService.getAllMovies());
        return "admin/movies";
    }

    @GetMapping("/movies/create")
    public String showCreateMovieForm(Model model) {
        model.addAttribute("movieDTO", new MovieRequestDTO());
        model.addAttribute("genres", genreRepository.findAll());
        model.addAttribute("statuses", MovieStatus.values());
        return "admin/movie-form";
    }

    @PostMapping(value = "/movies/create", consumes = "multipart/form-data")
    public String processCreateMovie(@Valid @ModelAttribute("movieDTO") MovieRequestDTO movieDTO,
                                     BindingResult result,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("genres", genreRepository.findAll());
            model.addAttribute("statuses", MovieStatus.values());
            return "admin/movie-form";
        }
        movieService.createMovie(movieDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm phim thành công!");
        return "redirect:/admin/movies";
    }

    @GetMapping("/movies/edit/{id}")
    public String showEditMovieForm(@PathVariable Long id, Model model) {
        model.addAttribute("movieDTO", movieService.getMovieForEdit(id));
        model.addAttribute("movieId", id);
        model.addAttribute("genres", genreRepository.findAll());
        model.addAttribute("statuses", MovieStatus.values());
        return "admin/movie-form";
    }

    @PostMapping(value = "/movies/edit/{id}", consumes = "multipart/form-data")
    public String processEditMovie(@PathVariable Long id,
                                   @Valid @ModelAttribute("movieDTO") MovieRequestDTO movieDTO,
                                   BindingResult result,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("movieId", id);
            model.addAttribute("genres", genreRepository.findAll());
            model.addAttribute("statuses", MovieStatus.values());
            return "admin/movie-form";
        }
        movieService.updateMovie(id, movieDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật phim thành công!");
        return "redirect:/admin/movies";
    }

    @PostMapping("/movies/delete/{id}")
    public String processDeleteMovie(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        movieService.deleteMovie(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa phim thành công!");
        return "redirect:/admin/movies";
    }

    @GetMapping("/showtimes")
    public String manageShowtimes() {
        return "admin/showtimes";
    }
}
