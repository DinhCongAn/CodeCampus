package com.codecampus.service;

import com.codecampus.entity.Registration;
import com.codecampus.repository.RegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class RevenueService {

    @Autowired private RegistrationRepository regRepo;

    public Map<String, Object> getRevenueStats(LocalDate startDate, LocalDate endDate, int page, int size) {
        Map<String, Object> stats = new HashMap<>();

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        // 1. Tổng doanh thu kỳ này
        BigDecimal currentRevenue = regRepo.sumRevenue(start, end);
        stats.put("totalRevenue", currentRevenue);

        // 2. So sánh với kỳ trước (Tính tăng trưởng %)
        // Ví dụ: Chọn xem 7 ngày -> So với 7 ngày trước đó
        long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        LocalDateTime prevStart = start.minusDays(daysDiff);
        LocalDateTime prevEnd = end.minusDays(daysDiff);

        BigDecimal prevRevenue = regRepo.sumRevenue(prevStart, prevEnd);

        double growth = 0.0;
        if (prevRevenue.compareTo(BigDecimal.ZERO) > 0) {
            growth = currentRevenue.subtract(prevRevenue)
                    .divide(prevRevenue, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100)).doubleValue();
        } else if (currentRevenue.compareTo(BigDecimal.ZERO) > 0) {
            growth = 100.0; // Tăng trưởng vô cực
        }
        stats.put("growth", growth);

        // 3. Biểu đồ theo ngày (Line Chart) - Lấp đầy ngày trống
        List<String> chartLabels = new ArrayList<>();
        List<BigDecimal> chartData = new ArrayList<>();

        List<Object[]> rawData = regRepo.getRevenueByDate(start, end);
        Map<String, BigDecimal> dataMap = new HashMap<>();
        for(Object[] row : rawData) {
            dataMap.put(row[0].toString(), (BigDecimal) row[1]);
        }

        LocalDate temp = startDate;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");

        while (!temp.isAfter(endDate)) {
            chartLabels.add(temp.format(fmt));
            // SQL trả về YYYY-MM-DD
            BigDecimal val = dataMap.getOrDefault(temp.toString(), BigDecimal.ZERO);
            chartData.add(val);
            temp = temp.plusDays(1);
        }
        stats.put("chartLabels", chartLabels);
        stats.put("chartData", chartData);

        // 4. Biểu đồ theo danh mục (Pie Chart)
        List<Object[]> catData = regRepo.getRevenueByCategory(start, end);
        List<String> catLabels = new ArrayList<>();
        List<BigDecimal> catValues = new ArrayList<>();
        for(Object[] row : catData) {
            catLabels.add((String) row[0]);
            catValues.add((BigDecimal) row[1]);
        }
        stats.put("catLabels", catLabels);
        stats.put("catValues", catValues);

        // 5. Bảng chi tiết giao dịch (Pagination)
        Page<Registration> transactions = regRepo.findSuccessfulOrders(start, end, PageRequest.of(page, size));
        stats.put("transactions", transactions);

        return stats;
    }
}