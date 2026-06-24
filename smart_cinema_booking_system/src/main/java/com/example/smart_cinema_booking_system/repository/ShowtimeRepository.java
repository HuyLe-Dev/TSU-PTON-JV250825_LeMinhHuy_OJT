package com.example.smart_cinema_booking_system.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.smart_cinema_booking_system.entity.Showtime;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    @Query("SELECT COUNT(s) FROM Showtime s WHERE s.room.roomId = :roomId AND s.startTime < :newEndTime AND s.endTime > :newStartTime AND s.status != 'CANCELLED'")
    long countOverlappingShowtimes(@Param("roomId") Long roomId, 
                                   @Param("newStartTime") LocalDateTime newStartTime, 
                                   @Param("newEndTime") LocalDateTime newEndTime);
}
