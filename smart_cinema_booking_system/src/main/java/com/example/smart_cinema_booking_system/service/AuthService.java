package com.example.smart_cinema_booking_system.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smart_cinema_booking_system.dto.request.LoginRequestDTO;
import com.example.smart_cinema_booking_system.dto.request.RegisterRequestDTO;
import com.example.smart_cinema_booking_system.entity.User;
import com.example.smart_cinema_booking_system.enums.Role;
import com.example.smart_cinema_booking_system.exception.BusinessException;
import com.example.smart_cinema_booking_system.repository.UserRepository;
import com.example.smart_cinema_booking_system.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void register(RegisterRequestDTO request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Tên đăng nhập đã tồn tại!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email đã được sử dụng!");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setEnabled(true);

        userRepository.save(user);
    }

    public String login(LoginRequestDTO request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("Sai tên đăng nhập hoặc mật khẩu!"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("Sai tên đăng nhập hoặc mật khẩu!");
        }

        if (!user.getEnabled()) {
            throw new BusinessException("Tài khoản đã bị khóa!");
        }

        return jwtTokenProvider.generateToken(user.getUsername(), user.getRole().name());
    }
}