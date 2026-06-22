package com.example.smart_cinema_booking_system.service;

import com.example.smart_cinema_booking_system.dto.request.ChangePasswordDTO;
import com.example.smart_cinema_booking_system.dto.request.ProfileUpdateDTO;
import com.example.smart_cinema_booking_system.dto.response.ProfileResponseDTO;
import com.example.smart_cinema_booking_system.entity.User;
import com.example.smart_cinema_booking_system.exception.BusinessException;
import com.example.smart_cinema_booking_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service xử lý logic nghiệp vụ cho Hồ sơ cá nhân (CORE-03).
 * - Xem hồ sơ
 * - Cập nhật hồ sơ
 * - Đổi mật khẩu
 */
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    /**
     * Lấy thông tin hồ sơ theo username (lấy từ JWT token).
     * Chuyển từ Entity sang DTO để không expose password.
     */
    public ProfileResponseDTO getProfile(String username) {
        User user = findUserByUsername(username);
        return mapToResponseDTO(user);
    }

    /**
     * Lấy thông tin hồ sơ dưới dạng ProfileUpdateDTO (cho form edit).
     */
    public ProfileUpdateDTO getProfileForEdit(String username) {
        User user = findUserByUsername(username);
        return mapToUpdateDTO(user);
    }

    /**
     * Cập nhật thông tin hồ sơ cá nhân.
     * Không cho phép thay đổi username và role.
     */
    @Transactional
    public void updateProfile(String username, ProfileUpdateDTO dto) {
        User user = findUserByUsername(username);

        // Kiểm tra email trùng với user khác
        userRepository.findByEmail(dto.getEmail()).ifPresent(existingUser -> {
            if (!existingUser.getId().equals(user.getId())) {
                throw new BusinessException("Email đã được sử dụng bởi tài khoản khác!");
            }
        });

        // Cập nhật các trường cho phép sửa
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());
        user.setDateOfBirth(dto.getDateOfBirth());
        user.setGender(dto.getGender());

        // Upload ảnh nếu có file mới
        if (dto.getAvatarFile() != null && !dto.getAvatarFile().isEmpty()) {
            String avatarUrl = cloudinaryService.uploadAvatar(dto.getAvatarFile(), username);
            user.setAvatarUrl(avatarUrl);
        }

        userRepository.save(user);
    }

    /**
     * Đổi mật khẩu.
     * 1. Verify mật khẩu hiện tại đúng
     * 2. Kiểm tra mật khẩu mới và xác nhận khớp nhau
     * 3. Hash mật khẩu mới và lưu
     */
    @Transactional
    public void changePassword(String username, ChangePasswordDTO dto) {
        User user = findUserByUsername(username);

        // 1. Xác minh mật khẩu hiện tại
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("Mật khẩu hiện tại không đúng!");
        }

        // 2. Kiểm tra mật khẩu mới không giống mật khẩu cũ (@PasswordMatch đã xử lý newPassword == confirmPassword ở tầng DTO)
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new BusinessException("Mật khẩu mới không được trùng với mật khẩu hiện tại!");
        }

        // 4. Hash và lưu mật khẩu mới
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    // ===== Private helper methods =====

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Không tìm thấy tài khoản!"));
    }

    private ProfileResponseDTO mapToResponseDTO(User user) {
        ProfileResponseDTO dto = new ProfileResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        dto.setAddress(user.getAddress());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setGender(user.getGender());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    private ProfileUpdateDTO mapToUpdateDTO(User user) {
        ProfileUpdateDTO dto = new ProfileUpdateDTO();
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAddress(user.getAddress());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setGender(user.getGender());
        return dto;
    }
}
