package com.example.smart_cinema_booking_system.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.smart_cinema_booking_system.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Upload avatar với public_id cố định theo username.
     * Nếu đã có ảnh cũ cùng public_id → Cloudinary tự động ghi đè, không tích lũy file thừa.
     */
    public String uploadAvatar(MultipartFile file, String username) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "avatars",
                            "public_id", username,
                            "overwrite", true,
                            "resource_type", "image"
                    ));
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new BusinessException("Upload ảnh thất bại: " + e.getMessage());
        }
    }
}
