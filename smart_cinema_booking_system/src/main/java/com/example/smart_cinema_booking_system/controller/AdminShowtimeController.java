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

import com.example.smart_cinema_booking_system.enums.MovieStatus;
import java.util.Arrays;

import com.example.smart_cinema_booking_system.dto.request.ShowtimeRequestDTO;
import com.example.smart_cinema_booking_system.exception.BusinessException;
import com.example.smart_cinema_booking_system.repository.MovieRepository;
import com.example.smart_cinema_booking_system.repository.RoomRepository;
import com.example.smart_cinema_booking_system.service.ShowtimeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/showtimes")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminShowtimeController {

    private final ShowtimeService showtimeService;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;

    @GetMapping
    public String manageShowtimes(Model model) {
        model.addAttribute("showtimes", showtimeService.getAllShowtimes());
        return "admin/showtimes";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("showtimeDTO", new ShowtimeRequestDTO());
        model.addAttribute("movies", movieRepository.findByStatusIn(Arrays.asList(MovieStatus.NOW_SHOWING, MovieStatus.COMING_SOON)));
        model.addAttribute("rooms", roomRepository.findAll());
        return "admin/showtime-form";
    }

    @PostMapping("/create")
    public String processCreate(@Valid @ModelAttribute("showtimeDTO") ShowtimeRequestDTO dto,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("movies", movieRepository.findByStatusIn(Arrays.asList(MovieStatus.NOW_SHOWING, MovieStatus.COMING_SOON)));
            model.addAttribute("rooms", roomRepository.findAll());
            return "admin/showtime-form";
        }

        try {
            showtimeService.createShowtime(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm suất chiếu thành công!");
            return "redirect:/admin/showtimes";
        } catch (BusinessException e) {
            model.addAttribute("movies", movieRepository.findByStatusIn(Arrays.asList(MovieStatus.NOW_SHOWING, MovieStatus.COMING_SOON)));
            model.addAttribute("rooms", roomRepository.findAll());
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/showtime-form";
        }
    }

    @PostMapping("/delete/{id}")
    public String processDelete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            showtimeService.deleteShowtime(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa suất chiếu thành công!");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/showtimes";
    }
}
