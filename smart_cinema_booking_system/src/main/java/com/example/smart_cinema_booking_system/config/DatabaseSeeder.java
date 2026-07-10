package com.example.smart_cinema_booking_system.config;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.smart_cinema_booking_system.entity.Genre;
import com.example.smart_cinema_booking_system.entity.Movie;
import com.example.smart_cinema_booking_system.entity.Room;
import com.example.smart_cinema_booking_system.entity.Seat;
import com.example.smart_cinema_booking_system.entity.User;
import com.example.smart_cinema_booking_system.enums.MovieStatus;
import com.example.smart_cinema_booking_system.enums.Role;
import com.example.smart_cinema_booking_system.enums.SeatType;
import com.example.smart_cinema_booking_system.repository.GenreRepository;
import com.example.smart_cinema_booking_system.repository.MovieRepository;
import com.example.smart_cinema_booking_system.repository.RoomRepository;
import com.example.smart_cinema_booking_system.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.seeder.enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseSeeder implements CommandLineRunner {

    private final GenreRepository genreRepository;
    private final RoomRepository roomRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedUsers();
        seedGenres();
        seedRoomsAndSeats();
        seedMovies();
    }

    private void seedUsers() {
        log.info("Checking and seeding Users...");
        String encodedPassword = passwordEncoder.encode("123456");

        if (!userRepository.existsByUsername("admin")) {
            createUser("admin", encodedPassword, "admin@smartcinema.vn", "Quản Trị Viên", "0901000001", Role.ADMIN);
            log.info("Admin user seeded.");
        }
        if (!userRepository.existsByUsername("staff")) {
            createUser("staff", encodedPassword, "staff@smartcinema.vn", "Nhân Viên Rạp", "0901000002", Role.STAFF);
        }
        if (!userRepository.existsByUsername("user1")) {
            createUser("user1", encodedPassword, "user1@gmail.com", "Nguyễn Văn A", "0901000003", Role.USER);
        }
        if (!userRepository.existsByUsername("user2")) {
            createUser("user2", encodedPassword, "user2@gmail.com", "Trần Thị B", "0901000004", Role.USER);
        }
        if (!userRepository.existsByUsername("huyle")) {
            createUser("huyle", encodedPassword, "huy.le@gmail.com", "Lê Minh Huy", "0901000005", Role.USER);
        }
    }

    private void createUser(String username, String encodedPassword, String email,
            String fullName, String phone, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(encodedPassword);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setRole(role);
        user.setEnabled(true);
        userRepository.save(user);
    }

    private void seedGenres() {
        if (genreRepository.count() == 0) {
            log.info("Seeding Genres...");
            List<String> genreNames = Arrays.asList(
                    "Hành động", "Kinh dị", "Tình cảm", "Hoạt hình",
                    "Khoa học viễn tưởng", "Hài", "Phiêu lưu", "Siêu anh hùng", "Kịch tính");
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
                room.setRoomName("Phòng " + r);
                room.setSeatsX(10);
                room.setSeatsY(5);
                room.setStatus(true);

                List<Seat> seats = new ArrayList<>();
                char[] rows = { 'A', 'B', 'C', 'D', 'E' };
                for (char row : rows) {
                    for (int col = 1; col <= 10; col++) {
                        Seat seat = new Seat();
                        seat.setRoom(room);
                        seat.setSeatName(row + String.valueOf(col));
                        seat.setStatus(true);

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
                roomRepository.save(room);
            }
            log.info("Rooms and Seats seeded successfully.");
        }
    }

    private void seedMovies() {
        log.info("Seeding Missing Movies from docs/Film.JSON...");

        List<Genre> allGenres = genreRepository.findAll();

        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File("docs/Film.JSON");
            if (!file.exists()) {
                log.error("Film.JSON not found at {}", file.getAbsolutePath());
                return;
            }
            JsonNode root = mapper.readTree(file);
            for (JsonNode node : root) {
                String title = node.has("Title") ? node.get("Title").asText() : "Unknown";
                if (movieRepository.existsByTitle(title)) {
                    continue;
                }
                String description = node.has("Plot") ? node.get("Plot").asText() : "";
                int duration = 120;
                if (node.has("Runtime")) {
                    String rt = node.get("Runtime").asText();
                    try {
                        duration = Integer.parseInt(rt.replaceAll("[^0-9]", ""));
                    } catch (Exception e) {}
                }
                
                LocalDate releaseDate = LocalDate.now();
                String language = node.has("Language") ? node.get("Language").asText() : "English";
                String posterUrl = node.has("Poster") ? node.get("Poster").asText() : "/images/logo.png";
                String ageRating = node.has("Rated") ? node.get("Rated").asText() : "P";
                
                MovieStatus status = MovieStatus.NOW_SHOWING;
                if (node.has("ComingSoon") && node.get("ComingSoon").asBoolean()) {
                    status = MovieStatus.COMING_SOON;
                }
                
                String trailerUrl = "https://www.youtube.com/embed/dQw4w9WgXcQ"; 
                List<Genre> genres = pickGenres(allGenres, 0, 1);
                
                String backdropUrl = posterUrl;
                if (node.has("Images") && node.get("Images").isArray() && node.get("Images").size() > 0) {
                    backdropUrl = node.get("Images").get(0).asText();
                }
                
                createMovie(title, description, duration, releaseDate, language, posterUrl, backdropUrl, trailerUrl, ageRating, status, genres);
            }
            log.info("Movies seeding from JSON completed.");
        } catch (Exception e) {
            log.error("Error seeding movies from JSON", e);
        }
    }

    private void createMovie(String title, String description, int duration,
            LocalDate releaseDate, String language, String posterUrl, String backdropUrl,
            String trailerUrl, String ageRating, MovieStatus status,
            List<Genre> genres) {
        if (!movieRepository.existsByTitle(title)) {
            Movie movie = new Movie();
            movie.setTitle(title);
            movie.setDescription(description);
            movie.setDuration(duration);
            movie.setReleaseDate(releaseDate);
            movie.setLanguage(language);
            movie.setPosterUrl(posterUrl);
            movie.setBackdropUrl(backdropUrl);
            movie.setTrailerUrl(trailerUrl);
            movie.setAgeRating(ageRating);
            movie.setStatus(status);
            movie.setGenres(genres);
            movieRepository.save(movie);
        }
    }

    private List<Genre> pickGenres(List<Genre> allGenres, int... indices) {
        List<Genre> picked = new ArrayList<>();
        for (int idx : indices) {
            if (idx < allGenres.size()) {
                picked.add(allGenres.get(idx));
            }
        }
        return picked;
    }
}
