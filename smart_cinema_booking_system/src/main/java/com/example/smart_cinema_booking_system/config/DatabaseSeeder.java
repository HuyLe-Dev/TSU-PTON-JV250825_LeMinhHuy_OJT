package com.example.smart_cinema_booking_system.config;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.smart_cinema_booking_system.entity.Genre;
import com.example.smart_cinema_booking_system.entity.Movie;
import com.example.smart_cinema_booking_system.entity.Room;
import com.example.smart_cinema_booking_system.entity.Seat;
import com.example.smart_cinema_booking_system.enums.MovieStatus;
import com.example.smart_cinema_booking_system.enums.SeatType;
import com.example.smart_cinema_booking_system.repository.GenreRepository;
import com.example.smart_cinema_booking_system.repository.MovieRepository;
import com.example.smart_cinema_booking_system.repository.RoomRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final GenreRepository genreRepository;
    private final RoomRepository roomRepository;
    private final MovieRepository movieRepository;

    @Override
    public void run(String... args) throws Exception {
        seedGenres();
        seedRoomsAndSeats();
        seedMovies();
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
        if (movieRepository.count() == 0) {
            log.info("Seeding Movies...");

            List<Genre> allGenres = genreRepository.findAll();
            // 0=Hành động, 1=Kinh dị, 2=Tình cảm, 3=Hoạt hình
            // 4=Khoa học viễn tưởng, 5=Hài, 6=Phiêu lưu, 7=Siêu anh hùng, 8=Kịch tính

            // ===== ĐANG CHIẾU =====
            createMovie("Beast - Quái Thú",
                    "Người cha góa vợ Dr. Nate Daniels cùng hai con gái tuổi teen đến khu bảo tồn Nam Phi. Chuyến đi chữa lành bỗng biến thành cuộc chiến sinh tồn khi một con sư tử khổng lồ bắt đầu săn đuổi họ.",
                    93, LocalDate.of(2025, 5, 10), "Tiếng Anh",
                    "/images/posters/beast.jpg",
                    "https://www.youtube.com/embed/oQMc7Sq36mI", "T16",
                    MovieStatus.NOW_SHOWING, pickGenres(allGenres, 0, 1));

            createMovie("Spider-Man: No Way Home",
                    "Danh tính bí mật của Peter Parker bị lộ, buộc anh phải nhờ Doctor Strange giúp đỡ. Khi phép thuật trục trặc, những kẻ thù nguy hiểm từ đa vũ trụ xuất hiện, Peter phải khám phá ý nghĩa thực sự của việc trở thành Người Nhện.",
                    148, LocalDate.of(2025, 4, 20), "Tiếng Anh",
                    "/images/posters/movie.png",
                    "https://www.youtube.com/embed/JfVOs4VSpmA", "T13",
                    MovieStatus.NOW_SHOWING, pickGenres(allGenres, 0, 7));

            createMovie("Strange World - Thế Giới Kỳ Lạ",
                    "Gia đình Clade - những nhà thám hiểm huyền thoại - dấn thân vào thế giới ngầm bí ẩn đầy sinh vật kỳ lạ. Họ phải vượt qua bất đồng để cứu lấy ngôi nhà và khám phá điều thực sự quan trọng.",
                    102, LocalDate.of(2025, 3, 15), "Tiếng Anh",
                    "/images/posters/strangeworld.jpg",
                    "https://www.youtube.com/embed/bKh2G73gCCs", "P",
                    MovieStatus.NOW_SHOWING, pickGenres(allGenres, 3, 6));

            createMovie("Upload - Thế Giới Ảo",
                    "Trong tương lai gần, con người có thể tải ý thức lên thiên đường ảo sau khi chết. Nathan bất ngờ qua đời và được upload lên Lakeview sang trọng - nơi anh phát hiện bí mật đen tối về cái chết của mình.",
                    110, LocalDate.of(2025, 5, 1), "Tiếng Anh",
                    "/images/posters/upload.jpg",
                    "https://www.youtube.com/embed/0ZfZj2bn_xg", "T16",
                    MovieStatus.NOW_SHOWING, pickGenres(allGenres, 4, 5));

            createMovie("Julie and the Phantoms",
                    "Sau khi mẹ qua đời, Julie - cô gái tuổi teen đam mê âm nhạc - tìm lại niềm đam mê khi lập ban nhạc cùng 3 hồn ma từ năm 1995. Cùng nhau, họ theo đuổi ước mơ âm nhạc xuyên thời gian.",
                    95, LocalDate.of(2025, 4, 5), "Tiếng Anh",
                    "/images/posters/julie_phantoms.jpg",
                    "https://www.youtube.com/embed/H14cBj0qO6Y", "P",
                    MovieStatus.NOW_SHOWING, pickGenres(allGenres, 2, 8));

            createMovie("Avatar: Con Đường Nước",
                    "Jake Sully và Neytiri phải bảo vệ gia đình khi mối đe dọa mới xuất hiện. Họ rời bỏ rừng xanh, đến với bộ tộc sống dưới nước của Pandora để tìm nơi trú ẩn an toàn.",
                    192, LocalDate.of(2025, 2, 28), "Tiếng Anh",
                    "/images/posters/moviebg1.jpg",
                    "https://www.youtube.com/embed/d9MyW72ELq0", "T13",
                    MovieStatus.NOW_SHOWING, pickGenres(allGenres, 4, 6));

            // ===== ĐANG CHIẾU (thêm để test slide) =====
            createMovie("Encanto - Vùng Đất Thần Kỳ",
                    "Cô gái Colombia phải đối mặt với sự thất vọng khi là thành viên duy nhất trong gia đình không có phép thuật. Liệu cô có thể cứu ngôi nhà thần kỳ?",
                    99, LocalDate.of(2025, 3, 20), "Tiếng Anh",
                    "/images/posters/strangeworld.jpg",
                    "https://www.youtube.com/embed/CaimKeDcudo", "P",
                    MovieStatus.NOW_SHOWING, pickGenres(allGenres, 3, 5));

            createMovie("Black Panther: Wakanda Bất Diệt",
                    "Nữ hoàng Ramonda, Shuri, M'Baku, Okoye và Dora Milaje chiến đấu bảo vệ Wakanda khỏi các thế lực bên ngoài sau cái chết của Vua T'Challa.",
                    161, LocalDate.of(2025, 5, 15), "Tiếng Anh",
                    "/images/posters/moviebg.jpg",
                    "https://www.youtube.com/embed/_Z3QKkl1WyM", "T13",
                    MovieStatus.NOW_SHOWING, pickGenres(allGenres, 0, 7));

            createMovie("Turning Red - Gấu Đỏ Biến Hình",
                    "Cô bé 13 tuổi Meilin biến thành một chú gấu trúc đỏ khổng lồ mỗi khi cảm xúc quá mãnh liệt. Cô phải học cách kiểm soát sức mạnh kỳ lạ này.",
                    100, LocalDate.of(2025, 4, 10), "Tiếng Anh",
                    "/images/posters/upload.jpg",
                    "https://www.youtube.com/embed/XdKzUbAiswE", "P",
                    MovieStatus.NOW_SHOWING, pickGenres(allGenres, 3, 5));

            createMovie("The Creator - Kẻ Sáng Tạo",
                    "Trong cuộc chiến giữa con người và AI, cựu binh Joshua được giao nhiệm vụ tiêu diệt vũ khí mạnh nhất. Nhưng khi phát hiện vũ khí đó là một AI hình trẻ em, mọi thứ thay đổi.",
                    133, LocalDate.of(2025, 6, 1), "Tiếng Anh",
                    "/images/posters/beast.jpg",
                    "https://www.youtube.com/embed/ex3C1-5Dhb8", "T13",
                    MovieStatus.NOW_SHOWING, pickGenres(allGenres, 4, 0));

            createMovie("Guardians of the Galaxy Vol. 3",
                    "Vẫn chưa nguôi ngoai sau mất mát Gamora, Peter Quill tập hợp đội để bảo vệ vũ trụ và một trong số họ - nhiệm vụ có thể là dấu chấm hết cho Guardians.",
                    150, LocalDate.of(2025, 5, 20), "Tiếng Anh",
                    "/images/posters/movie.png",
                    "https://www.youtube.com/embed/u3V5KDHRQvk", "T13",
                    MovieStatus.NOW_SHOWING, pickGenres(allGenres, 7, 6));

            createMovie("Super Mario Bros. Movie",
                    "Thợ sửa ống nước Mario phiêu lưu qua thế giới ngầm cùng Luigi để giải cứu công chúa. Hành trình đầy màu sắc và bất ngờ dành cho mọi lứa tuổi.",
                    92, LocalDate.of(2025, 4, 28), "Tiếng Anh",
                    "/images/posters/julie_phantoms.jpg",
                    "https://www.youtube.com/embed/TnGl01FkMMo", "P",
                    MovieStatus.NOW_SHOWING, pickGenres(allGenres, 3, 6));

            // ===== SẮP CHIẾU =====
            createMovie("The Batman - Hiệp Sĩ Bóng Đêm",
                    "Trong năm thứ hai chống tội phạm, Batman khám phá ra mạng lưới tham nhũng ở Gotham có liên quan đến gia đình mình, đồng thời đối mặt với kẻ giết người hàng loạt mang tên Riddler.",
                    176, LocalDate.of(2025, 8, 15), "Tiếng Anh",
                    "/images/posters/moviebg.jpg",
                    "https://www.youtube.com/embed/mqqft2x_Aa4", "T16",
                    MovieStatus.COMING_SOON, pickGenres(allGenres, 0, 7));

            createMovie("Wednesday - Cô Nàng Kỳ Quái",
                    "Wednesday Addams được gửi đến Học viện Nevermore, nơi cô cố gắng làm chủ năng lực ngoại cảm, ngăn chặn chuỗi giết chóc trong thị trấn và giải mã bí ẩn siêu nhiên liên quan đến cha mẹ cô 25 năm trước.",
                    120, LocalDate.of(2025, 9, 1), "Tiếng Anh",
                    "/images/posters/moviebg2.webp",
                    "https://www.youtube.com/embed/Di310WS8zLk", "T16",
                    MovieStatus.COMING_SOON, pickGenres(allGenres, 1, 5));

            createMovie("Puss in Boots: Điều Ước Cuối Cùng",
                    "Sau khi đốt hết 8 trong 9 mạng sống, Mèo Đi Hia lên đường tìm kiếm Điều Ước Cuối Cùng huyền thoại. Với thời gian cạn kiệt, chú mèo phải đối mặt kẻ thù mới trong hành trình chuộc lỗi đầy dũng cảm.",
                    102, LocalDate.of(2025, 7, 20), "Tiếng Anh",
                    "https://upload.wikimedia.org/wikipedia/en/7/78/Puss_in_Boots_The_Last_Wish_poster.jpg",
                    "https://www.youtube.com/embed/RqrXhwS33yc", "P",
                    MovieStatus.COMING_SOON, pickGenres(allGenres, 3, 6));

            createMovie("Oppenheimer - Cha Đẻ Bom Nguyên Tử",
                    "Câu chuyện về nhà khoa học J. Robert Oppenheimer và vai trò của ông trong việc phát triển bom nguyên tử. Bộ phim khắc họa cuộc đời đầy mâu thuẫn giữa thiên tài và bi kịch.",
                    180, LocalDate.of(2025, 10, 5), "Tiếng Anh",
                    "https://upload.wikimedia.org/wikipedia/en/4/4a/Oppenheimer_%28film%29.jpg",
                    "https://www.youtube.com/embed/uYPbbksJxIg", "T16",
                    MovieStatus.COMING_SOON, pickGenres(allGenres, 8, 0));

            createMovie("Aquaman: Vương Quốc Thất Lạc",
                    "Khi thế lực cổ đại trỗi dậy, Aquaman phải liên minh để bảo vệ Atlantis khỏi sự hủy diệt. Arthur đối mặt kẻ thù từ cả đất liền lẫn đại dương.",
                    124, LocalDate.of(2025, 8, 1), "Tiếng Anh",
                    "/images/posters/moviebg1.jpg",
                    "https://www.youtube.com/embed/UGc5Tzz19UY", "T13",
                    MovieStatus.COMING_SOON, pickGenres(allGenres, 7, 6));

            createMovie("Black Knight - Hiệp Sĩ Đen",
                    "Trong tương lai ô nhiễm nặng nề, những tài xế giao hàng tinh nhuệ gọi là Black Knight liều mạng vận chuyển vật tư. Khi một hiệp sĩ huyền thoại gặp người tị nạn, họ phải chống lại chế độ tham nhũng.",
                    110, LocalDate.of(2025, 9, 20), "Tiếng Hàn",
                    "/images/posters/beast.jpg",
                    "https://www.youtube.com/embed/UGc5Tzz19UY", "T16",
                    MovieStatus.COMING_SOON, pickGenres(allGenres, 0, 4));

            createMovie("Stranger Things 5 - Những Điều Kỳ Lạ",
                    "Khi một cậu bé biến mất, thị trấn nhỏ phát hiện bí mật liên quan đến thí nghiệm bí mật, thế lực siêu nhiên đáng sợ và một cô bé kỳ lạ. Mùa cuối cùng hứa hẹn bùng nổ.",
                    135, LocalDate.of(2025, 11, 15), "Tiếng Anh",
                    "/images/posters/moviebg2.webp",
                    "https://www.youtube.com/embed/b9EkMc79ZSU", "T16",
                    MovieStatus.COMING_SOON, pickGenres(allGenres, 4, 1));

            createMovie("Moana 2 - Hành Trình Của Moana",
                    "Moana lại ra khơi, đáp lời tiếng gọi từ tổ tiên để hành trình qua những vùng biển nguy hiểm đã bị lãng quên. Cùng bạn mới và cũ, cô đối mặt sức mạnh đại dương.",
                    100, LocalDate.of(2025, 12, 1), "Tiếng Anh",
                    "/images/posters/strangeworld.jpg",
                    "https://www.youtube.com/embed/hDZ7y8RP5HE", "P",
                    MovieStatus.COMING_SOON, pickGenres(allGenres, 3, 6));

            log.info("Movies seeded successfully.");
        }
    }

    private void createMovie(String title, String description, int duration,
            LocalDate releaseDate, String language, String posterUrl,
            String trailerUrl, String ageRating, MovieStatus status,
            List<Genre> genres) {
        Movie movie = new Movie();
        movie.setTitle(title);
        movie.setDescription(description);
        movie.setDuration(duration);
        movie.setReleaseDate(releaseDate);
        movie.setLanguage(language);
        movie.setPosterUrl(posterUrl);
        movie.setTrailerUrl(trailerUrl);
        movie.setAgeRating(ageRating);
        movie.setStatus(status);
        movie.setGenres(genres);
        movieRepository.save(movie);
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
