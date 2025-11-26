package com.codecampus.service;

import com.codecampus.dto.TopSubjectDTO;
import com.codecampus.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class DashboardService {

    @Autowired private RegistrationRepository regRepo;
    @Autowired private CourseRepository courseRepo;
    @Autowired private UserRepository userRepo;

    public Map<String, Object> getDashboardData(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> data = new HashMap<>();

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        // --- 1. CARD: TỔNG DOANH THU & TĂNG TRƯỞNG ---
        BigDecimal currentRevenue = regRepo.sumRevenue(start, end);
        data.put("totalRevenue", currentRevenue);

        // Tính kỳ trước để so sánh (Ví dụ: xem 7 ngày thì so với 7 ngày trước đó)
        long daysDiff = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        LocalDateTime prevStart = start.minusDays(daysDiff);
        LocalDateTime prevEnd = end.minusDays(daysDiff);
        BigDecimal prevRevenue = regRepo.sumRevenue(prevStart, prevEnd);

        if (prevRevenue.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal change = currentRevenue.subtract(prevRevenue)
                    .divide(prevRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
            data.put("revenueChange", change.doubleValue());
        } else {
            data.put("revenueChange", 100.0); // Tăng trưởng vô cực nếu kỳ trước = 0
        }

        // --- 2. CARD: MÔN HỌC ---
        data.put("newSubjectsAll", courseRepo.count()); // Tổng hiện có
        data.put("newSubjectsNew", courseRepo.countByCreatedAtBetween(start, end)); // Mới tạo

        // --- 3. CARD: ĐĂNG KÝ (REGISTRATIONS) ---
        data.put("newRegistrationsSubmitted", regRepo.countAllInPeriod(start, end));
        data.put("newRegistrationsSuccess", regRepo.countByStatus("COMPLETED", start, end));
        data.put("newRegistrationsCancelled", regRepo.countByStatus("CANCELLED", start, end));

        // --- 4. CARD: KHÁCH HÀNG (CUSTOMERS) ---
        data.put("newCustomersRegistered", userRepo.countByCreatedAtBetween(start, end));
        data.put("newCustomersBought", regRepo.countNewPayingCustomers(start, end));

        // --- 5. CHART: XU HƯỚNG ĐƠN HÀNG (LẤP ĐẦY NGÀY THIẾU) ---
        List<String> dates = new ArrayList<>();
        List<Long> successData = new ArrayList<>();
        List<Long> allData = new ArrayList<>();

        // Lấy dữ liệu thô từ DB
        Map<String, Long> mapSuccess = convertToMap(regRepo.countSuccessOrdersByDay(start, end));
        Map<String, Long> mapAll = convertToMap(regRepo.countOrdersByDay(start, end));

        LocalDate tempDate = startDate;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        // Loop từng ngày để đảm bảo biểu đồ liên tục
        while (!tempDate.isAfter(endDate)) {
            String sqlDateKey = tempDate.toString(); // Key format YYYY-MM-DD từ SQL
            String displayLabel = tempDate.format(formatter); // Label format dd/MM

            dates.add(displayLabel);
            successData.add(mapSuccess.getOrDefault(sqlDateKey, 0L));
            allData.add(mapAll.getOrDefault(sqlDateKey, 0L));

            tempDate = tempDate.plusDays(1);
        }

        data.put("orderTrendDates", dates);
        data.put("orderTrendSuccess", successData);
        data.put("orderTrendAll", allData);

        // --- 6. CHART: DOANH THU THEO DANH MỤC ---
        List<Object[]> catRevenue = regRepo.getRevenueByCategory(start, end);
        List<String> catNames = new ArrayList<>();
        List<BigDecimal> catValues = new ArrayList<>();

        for (Object[] row : catRevenue) {
            catNames.add((String) row[0]);
            catValues.add((BigDecimal) row[1]);
        }
        data.put("revenueCategories", catNames);
        data.put("revenueValues", catValues);

        // --- 7. TOP 5 MÔN HỌC ---
        List<TopSubjectDTO> topSubjects = regRepo.getTopSubjects(start, end, PageRequest.of(0, 5));
        data.put("topSubjects", topSubjects);

        return data;
    }

    // Helper: Convert List<Object[]> sang Map<DateString, Count>
    private Map<String, Long> convertToMap(List<Object[]> rawData) {
        Map<String, Long> map = new HashMap<>();
        for (Object[] row : rawData) {
            // row[0] có thể là java.sql.Date hoặc String tùy Driver DB, ta toString() cho chắc
            String dateKey = row[0].toString();
            Long count = ((Number) row[1]).longValue();
            map.put(dateKey, count);
        }
        return map;
    }
}