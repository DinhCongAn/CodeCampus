package com.codecampus.controller.admin;

import com.codecampus.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String showDashboard(
            // Mặc định lấy 7 ngày gần nhất nếu không chọn ngày
            @RequestParam(value = "start", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "end", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            Model model) {

        // 1. Xử lý ngày mặc định
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate == null) {
            startDate = endDate.minusDays(6); // 7 ngày bao gồm cả hôm nay
        }

        // 2. Lấy toàn bộ dữ liệu từ Service
        Map<String, Object> dashboardData = dashboardService.getDashboardData(startDate, endDate);

        // 3. Đẩy từng key trong Map ra Model để Thymeleaf dùng
        // (totalRevenue, orderTrendDates, topSubjects, ...)
        model.addAllAttributes(dashboardData);

        // 4. Đẩy lại ngày đã chọn để hiển thị trên input date
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "admin/dashboard";
    }
}