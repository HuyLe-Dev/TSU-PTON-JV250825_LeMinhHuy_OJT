package com.example.smart_cinema_booking_system.dto.request;

import com.example.smart_cinema_booking_system.validation.PasswordConfirmable;
import com.example.smart_cinema_booking_system.validation.PasswordMatch;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO nhận dữ liệu đổi mật khẩu.
 * Yêu cầu nhập mật khẩu cũ để xác minh trước khi đổi.
 */
@Data
@PasswordMatch
public class ChangePasswordDTO implements PasswordConfirmable {

    // PasswordConfirmable interface dùng getPassword() để so sánh với confirmPassword
    // Map sang newPassword để tái sử dụng @PasswordMatch validator
    @Override
    public String getPassword() {
        return newPassword;
    }

    @NotBlank(message = "Mật khẩu hiện tại không được để trống")
    private String currentPassword;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 6, message = "Mật khẩu mới phải có ít nhất 6 ký tự")
    private String newPassword;

    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String confirmPassword;
}
