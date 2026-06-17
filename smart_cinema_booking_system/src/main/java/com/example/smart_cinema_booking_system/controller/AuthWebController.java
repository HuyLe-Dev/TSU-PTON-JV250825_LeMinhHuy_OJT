package com.example.smart_cinema_booking_system.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.example.smart_cinema_booking_system.dto.request.LoginRequestDTO;
import com.example.smart_cinema_booking_system.dto.request.RegisterRequestDTO;
import com.example.smart_cinema_booking_system.service.AuthService;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthWebController {

    private final AuthService authService;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerDTO", new RegisterRequestDTO());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute("registerDTO") RegisterRequestDTO request,
            BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "auth/register";
        }
        try {
            authService.register(request);
            model.addAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/auth/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("loginDTO", new LoginRequestDTO());
        return "auth/login";
    }

    @PostMapping("/login")
    public String processLogin(@Valid @ModelAttribute("loginDTO") LoginRequestDTO request,
            BindingResult result, Model model, HttpServletResponse response) {
        if (result.hasErrors()) {
            return "auth/login";
        }
        try {
            String token = authService.login(request);

            // Sinh Cookie chứa JWT
            Cookie jwtCookie = new Cookie("JWT_TOKEN", token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(24 * 60 * 60); // 1 ngày
            response.addCookie(jwtCookie);

            return "redirect:/"; // Chuyển về trang chủ sau khi login thành công
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("JWT_TOKEN", null);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0); // Xóa Cookie
        response.addCookie(jwtCookie);
        return "redirect:/auth/login";
    }
}
