package com.codecampus.controller;

import com.codecampus.entity.*;
import com.codecampus.service.LabService;
import com.codecampus.service.LessonService;
import com.codecampus.service.MyCourseService;
import com.codecampus.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/learning")
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
    @GetMapping("/lab/{labId}")
    public String showLabPage(@PathVariable Integer labId,
                              @RequestParam(required = false) Integer lessonId,
                              @RequestParam(required = false, defaultValue = "false") boolean newAttempt,
                              Model model,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {

        User user = getCurrentUser(principal);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            LabAttempt attempt;

            if (newAttempt) {
                // CASE A: Yêu cầu tạo lượt làm mới hoàn toàn
                attempt = labService.createNewLabAttempt(labId, user);

                // Redirect để xóa tham số newAttempt=true khỏi URL (URL Clean up)
                String redirectUrl = "/learning/lab/" + labId;
                if (lessonId != null) {
                    redirectUrl += "?lessonId=" + lessonId;
                }
                return "redirect:" + redirectUrl;

            } else {
                // CASE B: Mặc định, tải lượt làm mới nhất hoặc tạo lượt đầu tiên
                Optional<LabAttempt> latestAttemptOpt = labService.getLatestLabAttempt(labId, user.getId());

                if (latestAttemptOpt.isPresent()) {
                    attempt = latestAttemptOpt.get(); // Lấy lượt mới nhất (dù đã chấm xong hay chưa)
                } else {
                    // Nếu chưa làm lần nào, tạo lượt đầu tiên
                    attempt = labService.createNewLabAttempt(labId, user);
                }
            }

            // 1. Lấy thông tin Lab và Course
            Lab lab = attempt.getLab();
            Lesson currentLesson = lessonService.findLessonByLabId(labId); // Cần LessonService có hàm này
            Course course = currentLesson.getCourse();

            // 2. (Kiểm tra bảo mật: User đã mua khóa học chưa)
            boolean hasAccess = myCourseService.isUserEnrolled(user.getId(), course.getId());
            if (!hasAccess) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền truy cập khóa học này.");
                return "redirect:/my-courses";
            }

            // 3. Lấy các bài học cho sidebar
            List<Lesson> allLessons = lessonService.getActiveLessonsByCourseId(course.getId());

            // 4. Đẩy tất cả dữ liệu ra Model
            model.addAttribute("attempt", attempt);
            model.addAttribute("lab", lab);
            model.addAttribute("course", course);
            model.addAttribute("allLessons", allLessons);
            model.addAttribute("currentLesson", currentLesson);

            // Cung cấp code hiện tại của lượt làm bài này cho Monaco Editor
            model.addAttribute("initialCode", attempt.getSubmittedContent() != null ? attempt.getSubmittedContent() : "");
            model.addAttribute("lessonId", lessonId); // Đẩy lessonId lại ra view

            return "learning/lab-view";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/my-courses";
        }
    }
}