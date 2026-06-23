package com.example.smart_cinema_booking_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.example.smart_cinema_booking_system.enums.SeatType;

@Entity
@Table(name = "seats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Seat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_id")
    private Long seatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "seat_name", length = 10, nullable = false)
    private String seatName;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", length = 20, nullable = false)
    private SeatType seatType;

    @Column(nullable = false)
    private Boolean status;
}
