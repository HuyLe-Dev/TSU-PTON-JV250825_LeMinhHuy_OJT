package com.example.smart_cinema_booking_system.service;

import com.example.smart_cinema_booking_system.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final BookingRepository bookingRepository;

    public List<BigDecimal> getMonthlyRevenueForCurrentYear() {
        int currentYear = LocalDate.now().getYear();
        List<Object[]> results = bookingRepository.findMonthlyRevenue(currentYear);

        // Khởi tạo mảng 12 tháng với giá trị 0
        List<BigDecimal> monthlyRevenue = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            monthlyRevenue.add(BigDecimal.ZERO);
        }

        // Đổ dữ liệu từ DB vào mảng
        for (Object[] row : results) {
            int month = (Integer) row[0]; // JDBC trả về số tháng (1-12)
            BigDecimal revenue = (BigDecimal) row[1];
            monthlyRevenue.set(month - 1, revenue);
        }

        return monthlyRevenue;
    }

    public Map<String, BigDecimal> getTop5MoviesRevenue() {
        List<Object[]> results = bookingRepository.findTop5MoviesRevenue(PageRequest.of(0, 5));
        
        return results.stream().collect(
                Collectors.toMap(
                        row -> (String) row[0],
                        row -> (BigDecimal) row[1],
                        (v1, v2) -> v1,
                        LinkedHashMap::new // Giữ thứ tự giảm dần
                )
        );
    }
}
