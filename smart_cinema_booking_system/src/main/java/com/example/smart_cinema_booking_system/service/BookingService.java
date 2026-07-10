package com.example.smart_cinema_booking_system.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smart_cinema_booking_system.dto.request.BookingRequestDTO;
import com.example.smart_cinema_booking_system.dto.response.BookingHistoryDTO;
import com.example.smart_cinema_booking_system.dto.response.SeatInfoDTO;
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
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
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
        
        // Lưu tên ghế vào Booking để history vẫn hiển thị khi ticket bị xóa
        String seatNames = selectedSeats.stream()
                .map(Seat::getSeatName)
                .collect(Collectors.joining(", "));
        booking.setBookedSeatNames(seatNames);

        // Due to unique constraint on Tickets (showtime_id, seat_id),
        // if another transaction committed the same seat, this save will throw
        // DataIntegrityViolationException.
        booking = bookingRepository.save(booking);

        return booking.getBookingId();
    }

    /**
     * Lưu PayPal Order ID vào booking (dùng khi tạo PayPal Order).
     */
    @Transactional
    public void updatePaypalOrderId(Long bookingId, String paypalOrderId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đơn đặt vé!"));
        booking.setPaypalOrderId(paypalOrderId);
        bookingRepository.save(booking);
    }

    /**
     * Xác nhận thanh toán thành công (chuyển status sang PAID).
     */
    @Transactional
    public void confirmPayment(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đơn đặt vé!"));
        booking.setBookingStatus(BookingStatus.PAID);
        bookingRepository.save(booking);
        log.info("Booking [{}] payment confirmed. Status → PAID.", bookingId);
    }

    /**
     * Tìm booking theo PayPal Order ID.
     */
    public Booking findByPaypalOrderId(String paypalOrderId) {
        return bookingRepository.findByPaypalOrderId(paypalOrderId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đơn đặt vé với PayPal Order: " + paypalOrderId));
    }

    /**
     * Tìm booking theo ID.
     */
    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đơn đặt vé!"));
    }

    public List<BookingHistoryDTO> getBookingHistory(String username) {
        List<Booking> bookings = bookingRepository.findByUser_UsernameOrderByBookingDateDesc(username);
        LocalDateTime now = LocalDateTime.now();

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

            // CORE-09: Lấy tên ghế từ trường bookedSeatNames thay vì từ tickets
            // (vì khi cancel, tickets có thể đã bị xóa khỏi CSDL để nhả chỗ)
            String seats = b.getBookedSeatNames();
            if (seats == null || seats.isEmpty()) {
                seats = b.getTickets().stream()
                        .map(t -> t.getSeat().getSeatName())
                        .collect(Collectors.joining(", "));
            }
            dto.setSeats(seats);

            // CORE-09: cho phép hủy nếu chưa CANCELLED và trước 24h so với giờ chiếu
            boolean canCancel = b.getBookingStatus() != BookingStatus.CANCELLED
                    && b.getShowtime().getStartTime().minusHours(24).isAfter(now);
            dto.setCancellable(canCancel);

            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * CORE-09: Hủy vé chủ động & Giải phóng ghế.
     */
    @Transactional
    public void cancelBooking(Long bookingId, String username) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đơn đặt vé!"));

        // Kiểm tra quyền sở hữu
        if (!booking.getUser().getUsername().equals(username)) {
            log.warn("User [{}] attempted to cancel booking [{}] owned by [{}]",
                    username, bookingId, booking.getUser().getUsername());
            throw new BusinessException("Bạn không có quyền hủy đơn này!");
        }

        // Kiểm tra trạng thái
        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException("Đơn vé này đã được hủy trước đó.");
        }

        // Kiểm tra thời gian: phải trước 24 giờ so với giờ chiếu
        LocalDateTime showtimeStart = booking.getShowtime().getStartTime();
        LocalDateTime cancelDeadline = showtimeStart.minusHours(24);

        if (LocalDateTime.now().isAfter(cancelDeadline)) {
            String deadlineStr = cancelDeadline.format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"));
            throw new BusinessException("Chỉ được hủy vé trước 24 giờ so với giờ chiếu. Hạn cuối: " + deadlineStr);
        }

        // Cập nhật trạng thái → CANCELLED
        booking.setBookingStatus(BookingStatus.CANCELLED);
        
        // XÓA các bản ghi Ticket trong Database để nhả UNIQUE CONSTRAINT (showtime_id, seat_id)
        // Việc này cho phép người khác đặt lại chính ghế đó mà không bị lỗi DataIntegrityViolation
        booking.getTickets().clear();
        
        bookingRepository.save(booking);

        log.info("Booking [{}] cancelled by user [{}]. Showtime [{}]. Seats released.",
                bookingId, username, booking.getShowtime().getShowtimeId());
    }
}
