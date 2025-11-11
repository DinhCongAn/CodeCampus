// src/main/java/com/codecampus/controller/MyCoursesController.java
package com.codecampus.controller;

import com.codecampus.entity.Registration;
import com.codecampus.entity.User;
import com.codecampus.service.RegistrationService;
import com.codecampus.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class MyCoursesController {

    @Autowired
    private UserService userService;

    @Autowired
    private RegistrationService registrationService;

    /**
     * Helper: Lấy thông tin User đang đăng nhập
     */
    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String email = authentication.getName();
        try {
            // Dùng UserService (như trong ProfileController)
            return userService.findUserByEmail(email);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Xử lý hiển thị trang "Khóa học của tôi"
     */
    @GetMapping("/my-courses")
    public String showMyCourses(Model model, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        // Nếu chưa đăng nhập, chuyển về trang login
        if (currentUser == null) {
            return "redirect:/login";
        }

        // 1. Gọi service để lấy danh sách khóa học
        // (Đây là hàm getCoursesByUserId() trong RegistrationService)
        List<Registration> myRegistrations = registrationService.getCoursesByUserId(currentUser.getId());

        // 2. Đẩy (Push) danh sách này ra Model với tên là "registrations"
        // (File HTML của bạn sẽ dùng th:each="reg : ${registrations}")
        model.addAttribute("registrations", myRegistrations);

        // 3. Trả về tên file HTML (my-courses.html)
        return "my-courses";
    }
}