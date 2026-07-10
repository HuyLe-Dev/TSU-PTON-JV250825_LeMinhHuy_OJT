package com.example.smart_cinema_booking_system.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.smart_cinema_booking_system.enums.BookingStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Booking extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;

    @Column(name = "booking_date")
    private LocalDateTime bookingDate;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status")
    private BookingStatus bookingStatus;

    @Column(name = "booked_seat_names", length = 255)
    private String bookedSeatNames;

    @Column(name = "paypal_order_id", length = 50)
    private String paypalOrderId;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Ticket> tickets = new ArrayList<>();
}
