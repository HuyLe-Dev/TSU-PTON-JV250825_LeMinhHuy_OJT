package com.example.smart_cinema_booking_system.controller;

import java.security.Principal;

import org.springframework.dao.DataIntegrityViolationException;
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

import com.example.smart_cinema_booking_system.dto.request.BookingRequestDTO;
import com.example.smart_cinema_booking_system.exception.BusinessException;
import com.example.smart_cinema_booking_system.repository.ShowtimeRepository;
import com.example.smart_cinema_booking_system.service.BookingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/user/book")
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class UserBookingController {

    private final BookingService bookingService;
    private final ShowtimeRepository showtimeRepository;

    @GetMapping("/{showtimeId}")
    public String showSeatSelection(@PathVariable Long showtimeId, Model model) {
        var showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy suất chiếu!"));

        model.addAttribute("showtime", showtime);
        model.addAttribute("room", showtime.getRoom());
        model.addAttribute("seatsInfo", bookingService.getSeatMap(showtimeId));
        
        BookingRequestDTO dto = new BookingRequestDTO();
        dto.setShowtimeId(showtimeId);
        model.addAttribute("bookingDTO", dto);
        
        return "user/seat-selection";
    }

    @PostMapping("/submit")
    public String submitBooking(@Valid @ModelAttribute("bookingDTO") BookingRequestDTO dto,
                                BindingResult result,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", result.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/user/book/" + dto.getShowtimeId();
        }

        try {
            bookingService.createBooking(principal.getName(), dto);
            redirectAttributes.addFlashAttribute("successMessage", "Đặt vé thành công! Bạn có thể xem chi tiết trong Lịch sử mua vé.");
            return "redirect:/";
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Rất tiếc! Ghế bạn chọn đã có người nhanh tay đặt trước. Vui lòng chọn ghế khác.");
            return "redirect:/user/book/" + dto.getShowtimeId();
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/user/book/" + dto.getShowtimeId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi hệ thống xảy ra, vui lòng thử lại sau.");
            return "redirect:/user/book/" + dto.getShowtimeId();
        }
    }
}
