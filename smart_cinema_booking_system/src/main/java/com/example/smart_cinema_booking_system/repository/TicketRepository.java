package com.example.smart_cinema_booking_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.smart_cinema_booking_system.entity.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("SELECT t.seat.seatId FROM Ticket t WHERE t.showtime.showtimeId = :showtimeId AND t.booking.bookingStatus != 'CANCELLED'")
    List<Long> findBookedSeatIdsByShowtimeId(@Param("showtimeId") Long showtimeId);
}
