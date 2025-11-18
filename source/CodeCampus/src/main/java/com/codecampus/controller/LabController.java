package com.codecampus.controller;

import com.codecampus.entity.*;
import com.codecampus.service.LabService;
import com.codecampus.service.LessonService;
import com.codecampus.service.MyCourseService; // Dùng MyCourseService để check access
import com.codecampus.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
public class LabController {

    @Autowired private LabService labService;
    @Autowired private UserService userService;
    @Autowired private MyCourseService myCourseService;
    @Autowired private LessonService lessonService;

    /**
     * Helper: Lấy thông tin User đang đăng nhập
     */
    private User getCurrentUser(Principal principal) {
        if (principal == null) return null;
        String email = principal.getName();
        try {
            return userService.findUserByEmail(email);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Hiển thị màn hình Lab (Màn hình 19)
     */
    @GetMapping("/learning/lab/{labId}")
    public String showLabPage(@PathVariable Integer labId,
                              @RequestParam(required = false) Integer lessonId,
                              Model model,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {

        User user = getCurrentUser(principal);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            // 1. Bắt đầu hoặc lấy lượt Lab (Attempt)
            LabAttempt attempt = labService.startOrGetLabAttempt(labId, user);

            // 2. Lấy thông tin Lab
            Lab lab = attempt.getLab();
            // Lấy Lesson và Course (Giả định LessonService có hàm tìm Lesson theo LabId)
            Lesson currentLesson = lessonService.findLessonByLabId(labId);
            Course course = currentLesson.getCourse();

            // 3. (Kiểm tra bảo mật: User đã mua khóa học chưa)
            boolean hasAccess = myCourseService.isUserEnrolled(user.getId(), course.getId());
            if (!hasAccess) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền truy cập khóa học này.");
                return "redirect:/my-courses";
            }

            // 4. Lấy các bài học cho sidebar
            List<Lesson> allLessons = lessonService.getLessonsByCourseId(course.getId());

            // 5. Đẩy tất cả dữ liệu ra Model
            model.addAttribute("attempt", attempt);
            model.addAttribute("lab", lab);
            model.addAttribute("course", course);
            model.addAttribute("allLessons", allLessons);
            model.addAttribute("currentLesson", currentLesson);
            // Mã Monaco Editor sẽ dùng attempt.submittedContent để load
            model.addAttribute("initialCode", attempt.getSubmittedContent() != null ? attempt.getSubmittedContent() : "");

            return "learning/lab-view";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/my-courses";
        }
    }
}