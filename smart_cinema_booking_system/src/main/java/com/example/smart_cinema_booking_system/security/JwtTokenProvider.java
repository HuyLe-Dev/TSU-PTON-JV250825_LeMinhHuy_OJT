package com.example.smart_cinema_booking_system.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // Chuỗi bí mật được lưu trong application.properties
    @Value("${app.jwt.secret}")
    private String JWT_SECRET;

    // Thời gian sống của Token: cấu hình trong properties (ms). Mặc định 86400000
    // ms = 1 ngày
    @Value("${app.jwt.expiration:86400000}")
    private long JWT_EXPIRATION;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes());
    }

    // 1. Tạo Token từ Username và Role
    public String generateToken(String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_EXPIRATION);

        return Jwts.builder()
                .setSubject(username)
                .claim("role", role) // Lưu Role vào payload của Token
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 2. Lấy Username từ Token
    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    // 3. Kiểm tra Token có hợp lệ không
    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            // Log lỗi ở đây nếu cần thiết
            return false;
        }
    }
}
