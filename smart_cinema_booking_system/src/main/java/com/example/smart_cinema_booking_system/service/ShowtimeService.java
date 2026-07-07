package com.example.smart_cinema_booking_system.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smart_cinema_booking_system.dto.request.ShowtimeRequestDTO;
import com.example.smart_cinema_booking_system.dto.response.ShowtimeResponseDTO;
import com.example.smart_cinema_booking_system.entity.Movie;
import com.example.smart_cinema_booking_system.enums.MovieStatus;
import com.example.smart_cinema_booking_system.entity.Room;
import com.example.smart_cinema_booking_system.entity.Showtime;
import com.example.smart_cinema_booking_system.enums.ShowtimeStatus;
import com.example.smart_cinema_booking_system.exception.BusinessException;
import com.example.smart_cinema_booking_system.repository.MovieRepository;
import com.example.smart_cinema_booking_system.repository.RoomRepository;
import com.example.smart_cinema_booking_system.repository.ShowtimeRepository;
import com.example.smart_cinema_booking_system.repository.TicketRepository;
import java.util.ArrayList;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final TicketRepository ticketRepository;

    @Transactional
    public List<ShowtimeResponseDTO> getAvailableShowtimesForMovie(Long movieId) {
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new BusinessException("Không tìm thấy phim!"));
        if (movie.getStatus() == MovieStatus.STOPPED) {
            return new ArrayList<>();
        }
        
        List<Showtime> showtimes = showtimeRepository.findByMovie_MovieIdAndStatusNotOrderByStartTimeAsc(movieId, ShowtimeStatus.CANCELLED);
        List<ShowtimeResponseDTO> dtos = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Showtime s : showtimes) {
            boolean changed = false;

            // 1. Kiểm tra Quá giờ
            if (s.getStartTime().isBefore(now) && s.getStatus() == ShowtimeStatus.OPEN) {
                s.setStatus(ShowtimeStatus.CLOSED);
                changed = true;
            }

            // 2. Kiểm tra Hết vé
            if (s.getStatus() == ShowtimeStatus.OPEN) {
                int bookedSeats = ticketRepository.findBookedSeatIdsByShowtimeId(s.getShowtimeId()).size();
                int totalSeats = s.getRoom().getSeats().size();
                if (bookedSeats >= totalSeats) {
                    s.setStatus(ShowtimeStatus.CLOSED);
                    changed = true;
                }
            }

            if (changed) {
                showtimeRepository.save(s);
            }

            dtos.add(mapToResponseDTO(s));
        }

        return dtos;
    }

    public List<ShowtimeResponseDTO> getAllShowtimes() {
        return showtimeRepository.findAll(Sort.by(Sort.Direction.DESC, "startTime")).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createShowtime(ShowtimeRequestDTO dto) {
        Movie movie = movieRepository.findById(dto.getMovieId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy phim!"));

        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy phòng chiếu!"));

        LocalDateTime startTime = dto.getStartTime();
        // Thời gian kết thúc = Thời gian bắt đầu + thời lượng phim + 15 phút dọn dẹp
        LocalDateTime endTime = startTime.plusMinutes(movie.getDuration() + 15);

        // Kiểm tra chống trùng lặp giờ chiếu
        long overlaps = showtimeRepository.countOverlappingShowtimes(room.getRoomId(), startTime, endTime);
        if (overlaps > 0) {
            throw new BusinessException("Phòng chiếu đã bị trùng lịch! Vui lòng chọn giờ khác.");
        }

        Showtime showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setStartTime(startTime);
        showtime.setEndTime(endTime);
        showtime.setTicketPrice(dto.getTicketPrice());
        showtime.setStatus(ShowtimeStatus.OPEN);

        showtimeRepository.save(showtime);
    }

    @Transactional
    public void deleteShowtime(Long id) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy suất chiếu!"));

        showtimeRepository.delete(showtime);
    }

    private ShowtimeResponseDTO mapToResponseDTO(Showtime showtime) {
        ShowtimeResponseDTO dto = new ShowtimeResponseDTO();
        dto.setShowtimeId(showtime.getShowtimeId());
        dto.setMovieTitle(showtime.getMovie().getTitle());
        dto.setMoviePosterUrl(showtime.getMovie().getPosterUrl());
        dto.setRoomName(showtime.getRoom().getRoomName());
        dto.setStartTime(showtime.getStartTime());
        dto.setEndTime(showtime.getEndTime());
        dto.setTicketPrice(showtime.getTicketPrice());
        dto.setStatus(showtime.getStatus());
        return dto;
    }
}
