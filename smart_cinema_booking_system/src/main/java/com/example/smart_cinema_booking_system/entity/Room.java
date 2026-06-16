package com.example.smart_cinema_booking_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Room extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "room_name", length = 50, nullable = false)
    private String roomName;

    @Column(name = "total_seats")
    private Integer totalSeats;

    @Column(name = "seats_x")
    private Integer seatsX;

    @Column(name = "seats_y")
    private Integer seatsY;

    // Sử dụng @JdbcTypeCode để map List trong Java thành kiểu JSON trong DB
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "vip_seats", columnDefinition = "json")
    private List<String> vipSeats;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "couple_seats", columnDefinition = "json")
    private List<String> coupleSeats;

    private Boolean status;
}
