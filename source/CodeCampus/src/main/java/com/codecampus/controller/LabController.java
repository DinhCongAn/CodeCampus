// src/main/java/com/codecampus/controller/LabController.java
package com.codecampus.controller;

import com.codecampus.entity.Course; // BỔ SUNG
import com.codecampus.entity.Lab; // BỔ SUNG
import com.codecampus.entity.LabAttempt;
import com.codecampus.entity.Lesson; // BỔ SUNG
import com.codecampus.entity.User;
import com.codecampus.service.LabService;
import com.codecampus.service.LessonService; // BỔ SUNG
import com.codecampus.service.RegistrationService; // BỔ SUNG
import com.codecampus.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // BỔ SUNG

import java.util.List; // BỔ SUNG

@Controller
public class LabController {

    @Autowired private LabService labService;
    @Autowired private UserService userService;
    @Autowired private RegistrationService registrationService; // BỔ SUNG
    @Autowired private LessonService lessonService; // BỔ SUNG

    /**
     * Helper: Lấy thông tin User đang đăng nhập
     */
    private User getCurrentUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        String email = auth.getName();
        try {
            return userService.findUserByEmail(email);
        } catch (Exception e) {
            return null; // Không tìm thấy
        }
    }

    /**
     * (Mới) Hiển thị màn hình Lab
     */
    @GetMapping("/learning/lab/{labId}")
    public String showLabPage(@PathVariable Integer labId,
                              Model model,
                              Authentication auth,
                              RedirectAttributes redirectAttributes) { // BỔ SUNG

        User user = getCurrentUser(auth);
        if (user == null) {
            return "redirect:/login"; // Phải đăng nhập
        }

        try {
            // 1. Bắt đầu hoặc lấy lượt Lab (Attempt)
            LabAttempt attempt = labService.startOrGetLabAttempt(labId, user);

            // 2. Lấy thông tin liên quan từ Attempt
            Lab lab = attempt.getLab();
            Lesson currentLesson = lab.getLesson();
            Course course = currentLesson.getCourse();

            // 3. (Kiểm tra bảo mật)
            // Kiểm tra xem user có quyền (COMPLETED) với khóa học này không
            boolean hasAccess = registrationService.hasUserRegistered(user.getId(), course.getId());
            if (!hasAccess) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền truy cập khóa học này.");
                return "redirect:/my-courses";
            }

            // 4. (Lấy allLessons cho sidebar)
            List<Lesson> allLessons = lessonService.getLessonsByCourseId(course.getId());

            // 5. Đẩy tất cả dữ liệu ra Model
            model.addAttribute("attempt", attempt);
            model.addAttribute("lab", lab);
            model.addAttribute("course", course);
            model.addAttribute("allLessons", allLessons);
            model.addAttribute("currentLesson", currentLesson);

            return "lab-view"; // Trả về file lab-view.html

        } catch (Exception e) {
            // Nếu Lab ID không tồn tại hoặc có lỗi khác
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/my-courses";
        }
    }
}