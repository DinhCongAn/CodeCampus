package com.codecampus.controller;

import com.codecampus.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final UserService userService;

    // Yêu cầu Spring "tiêm" UserService vào đây
    public DashboardController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model, Authentication authentication) {

        // 'authentication' là đối tượng mà Spring Security lưu trong session
        if (authentication == null) {
            return "redirect:/login"; // Lỗi (không nên xảy ra)
        }

        String fullName = "";
        Object principal = authentication.getPrincipal();

        if (principal instanceof OAuth2User) {
            // TRƯỜNG HỢP 1: Đăng nhập bằng Google
            OAuth2User oauth2User = (OAuth2User) principal;
            // Lấy thuộc tính "name" (fullName) mà Google trả về
            fullName = oauth2User.getAttribute("name");

        } else if (principal instanceof UserDetails) {
            // TRƯỜNG HỢP 2: Đăng nhập bằng Form (email/pass)
            UserDetails userDetails = (UserDetails) principal;
            String email = userDetails.getUsername();

            // Dùng email để tìm User entity trong DB
            com.codecampus.entity.User user = userService.findUserByEmail(email);

            if (user != null) {
                fullName = user.getFullName(); // Lấy fullName từ entity
            } else {
                fullName = email; // Dự phòng: Hiển thị email nếu không tìm thấy user
            }
        } else {
            // Trường hợp dự phòng khác
            fullName = principal.toString();
        }

        // Gửi biến fullName sang file HTML
        model.addAttribute("fullName", fullName);

        return "dashboard"; // Trả về templates/dashboard.html
    }
}