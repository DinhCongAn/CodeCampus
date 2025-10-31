package com.codecampus.config;

import com.codecampus.entity.User;
import com.codecampus.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice // Áp dụng cho TẤT CẢ các Controller
@Component
public class GlobalControllerAdvice {

    private final UserService userService;

    public GlobalControllerAdvice(UserService userService) {
        this.userService = userService;
    }

    /**
     * Phương thức này tự động thêm dữ liệu User vào Model cho mọi View
     * khi người dùng đã đăng nhập.
     */
    @ModelAttribute
    public void addCurrentUserAttributes(Model model, Authentication authentication) {

        // Chỉ chạy nếu người dùng đã đăng nhập và được xác thực
        if (authentication != null && authentication.isAuthenticated()) {

            String email = authentication.getName();
            User user = userService.findUserByEmail(email);

            String fullName = email; // Giá trị dự phòng
            String avatarUrl = null;
            String roleName = null;

            if (user != null) {
                fullName = user.getFullName();
                if (user.getAvatarUrl() != null) avatarUrl = user.getAvatarUrl();
                roleName = user.getRole().getName();
            } else {
                // Trường hợp đặc biệt (ví dụ: Google OAuth user chưa hoàn tất hồ sơ)
                fullName = email;
            }

            // Đặt các biến này vào Model. Chúng sẽ có sẵn trong file header.html
            model.addAttribute("fullName", fullName);
            model.addAttribute("avatarUrl", avatarUrl);
            model.addAttribute("roleName", roleName);
            model.addAttribute("email", email);
        }
    }
}