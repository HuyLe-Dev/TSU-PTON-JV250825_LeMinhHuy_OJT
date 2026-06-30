package com.example.smart_cinema_booking_system.controller;

import com.example.smart_cinema_booking_system.dto.request.ChangePasswordDTO;
import com.example.smart_cinema_booking_system.dto.request.ProfileUpdateDTO;
import com.example.smart_cinema_booking_system.dto.response.ProfileResponseDTO;
import com.example.smart_cinema_booking_system.enums.Gender;
import com.example.smart_cinema_booking_system.exception.BusinessException;
import com.example.smart_cinema_booking_system.service.ProfileService;
import com.example.smart_cinema_booking_system.service.BookingService;
import com.example.smart_cinema_booking_system.dto.response.BookingHistoryDTO;
import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller quản lý Hồ sơ cá nhân (CORE-03).
 * Dùng chung cho cả 3 role: USER, ADMIN, STAFF.
 * URL: /profile/** — yêu cầu đã đăng nhập (authenticated).
 */
@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final BookingService bookingService;

    /**
     * Xem hồ sơ cá nhân.
     */
    @GetMapping
    public String viewProfile(Authentication authentication, Model model) {
        String username = authentication.getName();
        ProfileResponseDTO profile = profileService.getProfile(username);
        model.addAttribute("profile", profile);
        return "profile/view";
    }

    /**
     * Hiển thị form chỉnh sửa hồ sơ.
     */
    @GetMapping("/edit")
    public String showEditForm(Authentication authentication, Model model) {
        String username = authentication.getName();
        ProfileUpdateDTO profileDTO = profileService.getProfileForEdit(username);
        model.addAttribute("profileDTO", profileDTO);
        model.addAttribute("genders", Gender.values());
        return "profile/edit";
    }

    /**
     * Xử lý cập nhật hồ sơ.
     */
    @PostMapping(value = "/edit", consumes = "multipart/form-data")
    public String processEdit(@Valid @ModelAttribute("profileDTO") ProfileUpdateDTO profileDTO,
                              BindingResult result,
                              Authentication authentication,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("genders", Gender.values());
            return "profile/edit";
        }

        try {
            String username = authentication.getName();
            profileService.updateProfile(username, profileDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ thành công!");
            return "redirect:/profile";
        } catch (BusinessException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("genders", Gender.values());
            return "profile/edit";
        }
    }

    /**
     * Hiển thị form đổi mật khẩu.
     */
    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model) {
        model.addAttribute("passwordDTO", new ChangePasswordDTO());
        return "profile/change-password";
    }

    /**
     * Xử lý đổi mật khẩu.
     */
    @PostMapping("/change-password")
    public String processChangePassword(@Valid @ModelAttribute("passwordDTO") ChangePasswordDTO passwordDTO,
                                        BindingResult result,
                                        Authentication authentication,
                                        Model model,
                                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "profile/change-password";
        }

        try {
            String username = authentication.getName();
            profileService.changePassword(username, passwordDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
            return "redirect:/profile";
        } catch (BusinessException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "profile/change-password";
        }
    }

    /**
     * Xem lịch sử đặt vé.
     */
    @GetMapping("/history")
    public String viewHistory(Authentication authentication, Model model) {
        String username = authentication.getName();
        List<BookingHistoryDTO> history = bookingService.getBookingHistory(username);
        model.addAttribute("history", history);
        return "profile/history";
    }
}
