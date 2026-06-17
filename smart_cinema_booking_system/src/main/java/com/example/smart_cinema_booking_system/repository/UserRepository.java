package com.example.smart_cinema_booking_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.smart_cinema_booking_system.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Tìm User theo tên đăng nhập (Phục vụ lúc Login và load UserDetails)
    Optional<User> findByUsername(String username);
    
    // Kiểm tra xem Username đã tồn tại chưa (Phục vụ lúc Register)
    boolean existsByUsername(String username);
    
    // Kiểm tra xem Email đã tồn tại chưa (Phục vụ lúc Register)
    boolean existsByEmail(String email);
}
