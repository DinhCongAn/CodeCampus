// src/main/java/com/codecampus/controller/MyCoursesController.java
package com.codecampus.controller;

import com.codecampus.entity.CourseCategory;
import com.codecampus.entity.Registration;
import com.codecampus.entity.User;
import com.codecampus.service.CourseService;
import com.codecampus.service.RegistrationService;
import com.codecampus.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class MyCoursesController {

    @Autowired
    private UserService userService;

    @Autowired
    private RegistrationService registrationService;
    @Autowired private CourseService courseService; // BỔ SUNG
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

    // --- BỔ SUNG: Helper tải dữ liệu Sidebar ---
    // (Logic này giống hệt CourseController)
    private void loadSidebarData(Model model) {
        List<CourseCategory> categories = courseService.getAllActiveCategories();
        model.addAttribute("courseCategories", categories);
        // (Thêm featuredCourses nếu fragment sidebar của bạn cần)
    }
    @GetMapping("/my-courses")
    public String showMyCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId, Model model, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        // 1. Tải dữ liệu cho Sidebar (Danh mục)
        loadSidebarData(model);

        // 2. Lấy danh sách khóa học (ĐÃ LỌC)
        List<Registration> myRegistrations = registrationService.getCoursesByUserId(
                currentUser.getId(),
                keyword,
                categoryId
        );

        // 3. Gửi danh sách ra HTML
        model.addAttribute("registrations", myRegistrations);

        // 4. Gửi lại các giá trị lọc để Sidebar hiển thị
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);

        return "my-courses";
    }
}