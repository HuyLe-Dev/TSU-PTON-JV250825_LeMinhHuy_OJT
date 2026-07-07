package com.example.smart_cinema_booking_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.smart_cinema_booking_system.entity.Booking;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser_UsernameOrderByBookingDateDesc(String username);

    @Query("SELECT MONTH(b.bookingDate), SUM(b.totalAmount) FROM Booking b WHERE b.bookingStatus != 'CANCELLED' AND YEAR(b.bookingDate) = :year GROUP BY MONTH(b.bookingDate) ORDER BY MONTH(b.bookingDate)")
    List<Object[]> findMonthlyRevenue(@Param("year") int year);

    @Query("SELECT b.showtime.movie.title, SUM(b.totalAmount) as totalRev FROM Booking b WHERE b.bookingStatus != 'CANCELLED' GROUP BY b.showtime.movie.title ORDER BY totalRev DESC")
    List<Object[]> findTop5MoviesRevenue(Pageable pageable);
}
