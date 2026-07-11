package com.example.smart_cinema_booking_system.service;

import com.example.smart_cinema_booking_system.entity.Booking;
import com.example.smart_cinema_booking_system.util.QRCodeGenerator;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import com.example.smart_cinema_booking_system.repository.BookingRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final BookingRepository bookingRepository;

    @Async
    @Transactional
    public void sendTicketEmail(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            log.error("Booking {} not found for email sending", bookingId);
            return;
        }
        if (booking.getUser().getEmail() == null || booking.getUser().getEmail().isEmpty()) {
            log.warn("User {} has no email, skipping ticket email.", booking.getUser().getUsername());
            return;
        }

        try {
            log.info("Starting async email sending for Booking ID: {}", booking.getBookingId());
            MimeMessage message = javaMailSender.createMimeMessage();
            // true = multipart message
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // TẠM THỜI GỬI TẤT CẢ VÉ VỀ EMAIL CỦA BẠN ĐỂ TEST
            // helper.setTo("ongconoizzz@gmail.com");
            helper.setTo(booking.getUser().getEmail()); // Code gốc
            helper.setSubject("Smart Cinema - Vé của bạn đã được xác nhận!");

            // Prepare dynamic data for Thymeleaf
            Context context = new Context();
            context.setVariable("bookingId", booking.getBookingId());
            context.setVariable("movieTitle", booking.getShowtime().getMovie().getTitle());
            context.setVariable("roomName", booking.getShowtime().getRoom().getRoomName());
            context.setVariable("showtimeStart",
                    booking.getShowtime().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")));

            String seats = booking.getBookedSeatNames();
            if (seats == null || seats.isEmpty()) {
                seats = booking.getTickets().stream()
                        .map(t -> t.getSeat().getSeatName())
                        .collect(Collectors.joining(", "));
            }
            context.setVariable("seats", seats);
            context.setVariable("totalAmount", booking.getTotalAmount());
            context.setVariable("paymentMethod", booking.getPaymentMethod());
            context.setVariable("username", booking.getUser().getUsername());

            // Render HTML template
            String htmlContent = templateEngine.process("email/ticket", context);
            helper.setText(htmlContent, true);

            // Generate QR Code
            String qrContent = String.format("BookingID:%d|User:%s|Movie:%s|Time:%s|Seats:%s",
                    booking.getBookingId(),
                    booking.getUser().getUsername(),
                    booking.getShowtime().getMovie().getTitle(),
                    booking.getShowtime().getStartTime().toString(),
                    seats);

            byte[] qrCodeImage = QRCodeGenerator.generateQRCodeImage(qrContent, 250, 250);

            if (qrCodeImage != null) {
                // Attach as inline image
                helper.addInline("qrCodeImage", new ByteArrayResource(qrCodeImage), "image/png");
            }

            javaMailSender.send(message);
            log.info("Successfully sent ticket email to {}", booking.getUser().getEmail());

        } catch (Exception e) {
            log.error("Failed to send ticket email to {}", booking.getUser().getEmail(), e);
        }
    }
}
