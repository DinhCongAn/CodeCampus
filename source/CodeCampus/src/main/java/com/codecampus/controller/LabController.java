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

    // Service xử lý logic liên quan tới Lab & LabAttempt
    @Autowired private LabService labService;

    // Service xử lý User (lấy user đang đăng nhập)
    @Autowired private UserService userService;

    // Service kiểm tra user đã đăng ký / mua khóa học hay chưa
    @Autowired private MyCourseService myCourseService;

    // Service xử lý Lesson (dùng cho sidebar, prev/next)
    @Autowired private LessonService lessonService;

    /**
     * Helper method
     * Mục đích: Lấy User hiện tại từ Principal
     * - Tránh lặp code trong các controller method
     * - Nếu chưa đăng nhập hoặc lỗi → trả về null
     */
    private User getCurrentUser(Principal principal) {
        if (principal == null) return null;
        String email = principal.getName();
        try {
            return userService.findUserByEmail(email);
        } catch (Exception e) {
            // Bắt mọi exception để tránh crash controller
            return null;
        }
    }

    /**
     * Hiển thị màn hình Lab (Màn hình 19)
     * URL: /learning/lab/{labId}
     *
     * Chức năng chính:
     * - Lấy hoặc tạo LabAttempt
     * - Kiểm tra quyền truy cập khóa học
     * - Chuẩn bị dữ liệu sidebar + lesson navigation
     * - Render trang lab-view
     */
    @GetMapping("/lab/{labId}")
    public String showLabPage(@PathVariable Integer labId,
                              @RequestParam(required = false) Integer lessonId,
                              @RequestParam(required = false, defaultValue = "false") boolean newAttempt,
                              Model model,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {

        // Lấy user hiện tại
        User user = getCurrentUser(principal);

        // Nếu chưa đăng nhập → redirect về login
        if (user == null) {
            return "redirect:/login";
        }

        try {
            LabAttempt attempt;

            if (newAttempt) {
                // CASE A:
                // User chủ động yêu cầu tạo lượt làm mới hoàn toàn (Redo / Làm lại từ đầu)
                attempt = labService.createNewLabAttempt(labId, user);

                // Redirect để làm sạch URL (loại bỏ ?newAttempt=true)
                String redirectUrl = "/learning/lab/" + labId;
                if (lessonId != null) {
                    redirectUrl += "?lessonId=" + lessonId;
                }
                return "redirect:" + redirectUrl;

            } else {
                // CASE B:
                // Mặc định: Lấy lượt làm gần nhất
                // Nếu chưa từng làm → tạo lượt đầu tiên
                Optional<LabAttempt> latestAttemptOpt =
                        labService.getLatestLabAttempt(labId, user.getId());

                if (latestAttemptOpt.isPresent()) {
                    // Có lượt làm trước đó → dùng lại
                    attempt = latestAttemptOpt.get();
                } else {
                    // Chưa từng làm → tạo lượt mới
                    attempt = labService.createNewLabAttempt(labId, user);
                }
            }

            // 1. Lấy thông tin Lab từ Attempt
            Lab lab = attempt.getLab();

            // 2. Xác định Lesson hiện tại dựa trên Lab
            // (Lab luôn thuộc về 1 Lesson)
            Lesson currentLesson = lessonService.findLessonByLabId(labId);

            // 3. Lấy Course từ Lesson
            Course course = currentLesson.getCourse();

            // 4. Kiểm tra bảo mật:
            // User phải đã đăng ký / mua khóa học thì mới được truy cập Lab
            boolean hasAccess =
                    myCourseService.isUserEnrolled(user.getId(), course.getId());

            if (!hasAccess) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "Bạn không có quyền truy cập khóa học này."
                );
                return "redirect:/my-courses";
            }

            // 5. Lấy toàn bộ Lesson của Course (phục vụ Sidebar + Prev/Next)
            List<Lesson> allLessons =
                    lessonService.getActiveLessonsByCourseId(course.getId());

            // 6. Tính toán Lesson trước / Lesson sau
            // Logic:
            // - Tìm index của Lesson hiện tại trong danh sách
            // - Dựa vào index để xác định prev / next
            Lesson prevLesson = null;
            Lesson nextLesson = null;
            int currentIndex = -1;

            Long currentLessonId = currentLesson.getId();

            for (int i = 0; i < allLessons.size(); i++) {
                if (allLessons.get(i).getId().equals(currentLessonId)) {
                    currentIndex = i;
                    break;
                }
            }

            // Nếu không phải bài đầu → có bài trước
            if (currentIndex > 0) {
                prevLesson = allLessons.get(currentIndex - 1);
            }

            // Nếu không phải bài cuối → có bài sau
            if (currentIndex != -1 && currentIndex < allLessons.size() - 1) {
                nextLesson = allLessons.get(currentIndex + 1);
            }

            // 7. Đẩy dữ liệu điều hướng Lesson ra View
            model.addAttribute("prevLesson", prevLesson);
            model.addAttribute("nextLesson", nextLesson);

            // 8. Đẩy dữ liệu chính của màn hình Lab
            model.addAttribute("attempt", attempt);
            model.addAttribute("lab", lab);
            model.addAttribute("course", course);
            model.addAttribute("allLessons", allLessons);
            model.addAttribute("currentLesson", currentLesson);

            // 9. Cung cấp code hiện tại cho Monaco Editor
            // Nếu chưa có bài nộp → truyền chuỗi rỗng
            model.addAttribute(
                    "initialCode",
                    attempt.getSubmittedContent() != null
                            ? attempt.getSubmittedContent()
                            : ""
            );

            // 10. Truyền lessonId ra view (phục vụ link / back / navigation)
            model.addAttribute("lessonId", lessonId);

            // 11. Render màn hình Lab
            return "learning/lab-view";

        } catch (Exception e) {
            // Bắt mọi exception để tránh lỗi trắng trang
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Lỗi: " + e.getMessage()
            );
            return "redirect:/my-courses";
        }
    }
}
