package com.example.smart_cinema_booking_system.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smart_cinema_booking_system.dto.request.BookingRequestDTO;
import com.example.smart_cinema_booking_system.dto.response.SeatInfoDTO;
import com.example.smart_cinema_booking_system.dto.response.BookingHistoryDTO;
import com.example.smart_cinema_booking_system.entity.Booking;
import com.example.smart_cinema_booking_system.entity.Seat;
import com.example.smart_cinema_booking_system.entity.Showtime;
import com.example.smart_cinema_booking_system.entity.Ticket;
import com.example.smart_cinema_booking_system.entity.User;
import com.example.smart_cinema_booking_system.enums.BookingStatus;
import com.example.smart_cinema_booking_system.enums.SeatType;
import com.example.smart_cinema_booking_system.exception.BusinessException;
import com.example.smart_cinema_booking_system.repository.BookingRepository;
import com.example.smart_cinema_booking_system.repository.SeatRepository;
import com.example.smart_cinema_booking_system.repository.ShowtimeRepository;
import com.example.smart_cinema_booking_system.repository.TicketRepository;
import com.example.smart_cinema_booking_system.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;

    public List<SeatInfoDTO> getSeatMap(Long showtimeId) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy suất chiếu!"));

        List<Long> bookedSeatIds = ticketRepository.findBookedSeatIdsByShowtimeId(showtimeId);
        List<Seat> allSeats = showtime.getRoom().getSeats();

        return allSeats.stream().map(seat -> {
            boolean isBooked = bookedSeatIds.contains(seat.getSeatId());
            return new SeatInfoDTO(seat.getSeatId(), seat.getSeatName(), seat.getSeatType().name(), isBooked);
        }).collect(Collectors.toList());
    }

    @Transactional
    public Long createBooking(String username, BookingRequestDTO dto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Người dùng không tồn tại"));

        Showtime showtime = showtimeRepository.findById(dto.getShowtimeId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy suất chiếu!"));

        if (showtime.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Suất chiếu đã bắt đầu hoặc kết thúc, không thể đặt vé.");
        }

        List<Seat> selectedSeats = seatRepository.findAllById(dto.getSeatIds());
        if (selectedSeats.size() != dto.getSeatIds().size()) {
            throw new BusinessException("Một số ghế không tồn tại hoặc không hợp lệ.");
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setShowtime(showtime);
        booking.setBookingDate(LocalDateTime.now());
        booking.setPaymentMethod(dto.getPaymentMethod());
        booking.setBookingStatus(BookingStatus.PENDING);

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Seat seat : selectedSeats) {
            BigDecimal seatPrice = showtime.getTicketPrice();
            if (seat.getSeatType() == SeatType.VIP) {
                seatPrice = seatPrice.add(new BigDecimal("20000"));
            } else if (seat.getSeatType() == SeatType.COUPLE) {
                seatPrice = seatPrice.add(new BigDecimal("50000"));
            }

            Ticket ticket = new Ticket();
            ticket.setBooking(booking);
            ticket.setShowtime(showtime);
            ticket.setSeat(seat);
            ticket.setPrice(seatPrice);

            booking.getTickets().add(ticket);
            totalAmount = totalAmount.add(seatPrice);
        }

        booking.setTotalAmount(totalAmount);
        
        // Due to unique constraint on Tickets (showtime_id, seat_id), 
        // if another transaction committed the same seat, this save will throw DataIntegrityViolationException.
        booking = bookingRepository.save(booking);
        
        return booking.getBookingId();
    }

    public List<BookingHistoryDTO> getBookingHistory(String username) {
        List<Booking> bookings = bookingRepository.findByUser_UsernameOrderByBookingDateDesc(username);
        return bookings.stream().map(b -> {
            BookingHistoryDTO dto = new BookingHistoryDTO();
            dto.setBookingId(b.getBookingId());
            dto.setMovieTitle(b.getShowtime().getMovie().getTitle());
            dto.setPosterUrl(b.getShowtime().getMovie().getPosterUrl());
            dto.setRoomName(b.getShowtime().getRoom().getRoomName());
            dto.setShowtimeStart(b.getShowtime().getStartTime());
            dto.setBookingDate(b.getBookingDate());
            dto.setTotalAmount(b.getTotalAmount());
            dto.setPaymentMethod(b.getPaymentMethod());
            dto.setStatus(b.getBookingStatus());
            
            String seats = b.getTickets().stream()
                    .map(t -> t.getSeat().getSeatName())
                    .collect(Collectors.joining(", "));
            dto.setSeats(seats);
            return dto;
        }).collect(Collectors.toList());
    }
}
