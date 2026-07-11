package com.example.smart_cinema_booking_system.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.smart_cinema_booking_system.entity.Booking;
import com.example.smart_cinema_booking_system.enums.BookingStatus;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser_UsernameOrderByBookingDateDesc(String username);

    @Query("SELECT MONTH(b.bookingDate), SUM(b.totalAmount) FROM Booking b WHERE b.bookingStatus != 'CANCELLED' AND YEAR(b.bookingDate) = :year GROUP BY MONTH(b.bookingDate) ORDER BY MONTH(b.bookingDate)")
    List<Object[]> findMonthlyRevenue(@Param("year") int year);

    @Query("SELECT b.showtime.movie.title, SUM(b.totalAmount) as totalRev FROM Booking b WHERE b.bookingStatus != 'CANCELLED' GROUP BY b.showtime.movie.title ORDER BY totalRev DESC")
    List<Object[]> findTop5MoviesRevenue(Pageable pageable);

    Optional<Booking> findByPaypalOrderId(String paypalOrderId);

    /**
     * Tìm các booking PENDING đã quá hạn thanh toán (dùng cho scheduler auto-cancel).
     * Chỉ tìm booking có paymentMethod = PAYPAL và bookingDate trước thời điểm deadline.
     */
    List<Booking> findByBookingStatusAndPaymentMethodAndBookingDateBefore(
            BookingStatus status, String paymentMethod, LocalDateTime deadline);

    long countByBookingStatusAndBookingDateBetween(BookingStatus status, LocalDateTime start, LocalDateTime end);
}
