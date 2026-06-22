package com.example.smart_cinema_booking_system.dto.response;

import com.example.smart_cinema_booking_system.enums.Gender;
import com.example.smart_cinema_booking_system.enums.Role;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO hiển thị thông tin hồ sơ cá nhân.
 * KHÔNG chứa password hay thông tin nhạy cảm.
 */
@Data
public class ProfileResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String address;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String avatarUrl;
    private Role role;
    private LocalDateTime createdAt;
}
