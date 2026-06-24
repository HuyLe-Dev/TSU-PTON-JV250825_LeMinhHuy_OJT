package com.example.smart_cinema_booking_system.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.smart_cinema_booking_system.entity.Genre;
import com.example.smart_cinema_booking_system.entity.Room;
import com.example.smart_cinema_booking_system.entity.Seat;
import com.example.smart_cinema_booking_system.enums.SeatType;
import com.example.smart_cinema_booking_system.repository.GenreRepository;
import com.example.smart_cinema_booking_system.repository.RoomRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final GenreRepository genreRepository;
    private final RoomRepository roomRepository;

    @Override
    public void run(String... args) throws Exception {
        seedGenres();
        seedRoomsAndSeats();
    }

    private void seedGenres() {
        if (genreRepository.count() == 0) {
            log.info("Seeding Genres...");
            List<String> genreNames = Arrays.asList("Hành động", "Tình cảm", "Hài", "Kinh dị", "Viễn tưởng");
            for (String name : genreNames) {
                Genre genre = new Genre();
                genre.setGenreName(name);
                genreRepository.save(genre);
            }
            log.info("Genres seeded successfully.");
        }
    }

    private void seedRoomsAndSeats() {
        if (roomRepository.count() == 0) {
            log.info("Seeding Rooms and Seats...");
            for (int r = 1; r <= 3; r++) {
                Room room = new Room();
                room.setRoomName("Room " + r);
                room.setSeatsX(10); // 10 cột
                room.setSeatsY(5);  // 5 hàng (A, B, C, D, E)
                room.setStatus(true);
                
                List<Seat> seats = new ArrayList<>();
                char[] rows = {'A', 'B', 'C', 'D', 'E'};
                for (char row : rows) {
                    for (int col = 1; col <= 10; col++) {
                        Seat seat = new Seat();
                        seat.setRoom(room);
                        seat.setSeatName(row + String.valueOf(col));
                        seat.setStatus(true);
                        
                        // Hàng E là ghế đôi (COUPLE), Hàng D là ghế VIP, còn lại STANDARD
                        if (row == 'E') {
                            seat.setSeatType(SeatType.COUPLE);
                        } else if (row == 'D') {
                            seat.setSeatType(SeatType.VIP);
                        } else {
                            seat.setSeatType(SeatType.STANDARD);
                        }
                        seats.add(seat);
                    }
                }
                room.setSeats(seats);
                roomRepository.save(room); // CascadeType.ALL sẽ tự động lưu Seats
            }
            log.info("Rooms and Seats seeded successfully.");
        }
    }
}
