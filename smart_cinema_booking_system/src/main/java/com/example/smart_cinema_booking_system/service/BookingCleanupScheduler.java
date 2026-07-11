package com.example.smart_cinema_booking_system.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.smart_cinema_booking_system.config.PayPalConfig;
import com.example.smart_cinema_booking_system.entity.Booking;
import com.example.smart_cinema_booking_system.enums.BookingStatus;
import com.example.smart_cinema_booking_system.repository.BookingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduled task: tự động hủy các booking PayPal PENDING quá thời gian cho phép.
 * Chạy mỗi phút, kiểm tra booking có bookingDate > 15 phút trước → cancel + nhả ghế.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookingCleanupScheduler {

    private final BookingRepository bookingRepository;
    private final PayPalConfig payPalConfig;

    /**
     * Chạy mỗi 60 giây.
     * Tìm booking PENDING đã quá paymentTimeoutMinutes (mặc định 15 phút) (áp dụng cho mọi hình thức thanh toán).
     * Hủy booking và xóa tickets để nhả ghế.
     */
    @Scheduled(fixedRate = 60000) // 60 giây
    @Transactional
    public void cancelExpiredPendingBookings() {
        int timeoutMinutes = payPalConfig.getPaymentTimeoutMinutes();
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(timeoutMinutes);

        List<Booking> expiredBookings = bookingRepository
                .findByBookingStatusAndBookingDateBefore(BookingStatus.PENDING, deadline);

        if (expiredBookings.isEmpty()) {
            return;
        }

        for (Booking booking : expiredBookings) {
            booking.setBookingStatus(BookingStatus.CANCELLED);
            // Xóa tickets để nhả UNIQUE CONSTRAINT (showtime_id, seat_id)
            booking.getTickets().clear();
            bookingRepository.save(booking);

            log.info("Auto-cancelled expired booking. BookingId: {}, Method: {}, User: {}, BookingDate: {}, Timeout: {} minutes",
                    booking.getBookingId(),
                    booking.getPaymentMethod(),
                    booking.getUser().getUsername(),
                    booking.getBookingDate(),
                    timeoutMinutes);
        }

        log.info("Cleanup completed. {} expired booking(s) cancelled.", expiredBookings.size());
    }
}
