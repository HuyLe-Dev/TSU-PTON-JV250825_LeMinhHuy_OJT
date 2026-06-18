package com.example.smart_cinema_booking_system.validation;

import com.example.smart_cinema_booking_system.dto.request.RegisterRequestDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator cho annotation @PasswordMatch.
 * So sánh password và confirmPassword trong RegisterRequestDTO.
 */
public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, RegisterRequestDTO> {

    @Override
    public boolean isValid(RegisterRequestDTO dto, ConstraintValidatorContext context) {
        if (dto.getPassword() == null || dto.getConfirmPassword() == null) {
            return false;
        }

        boolean isMatch = dto.getPassword().equals(dto.getConfirmPassword());

        if (!isMatch) {
            // Gắn lỗi vào trường confirmPassword để hiển thị trên form
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Mật khẩu xác nhận không khớp")
                    .addPropertyNode("confirmPassword")
                    .addConstraintViolation();
        }

        return isMatch;
    }
}
