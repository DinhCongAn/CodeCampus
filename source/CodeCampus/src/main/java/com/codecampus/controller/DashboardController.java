package com.codecampus.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        // Interceptor đã kiểm tra, nên session chắc chắn có
        String fullName = (String) session.getAttribute("loggedInUserFullName");
        model.addAttribute("fullName", fullName);

        return "dashboard"; // templates/dashboard.html
    }
}