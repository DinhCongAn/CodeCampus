// src/main/java/com/codecampus/controller/LessonController.java
package com.codecampus.controller;

import com.codecampus.entity.*;
import com.codecampus.service.LessonService;
import com.codecampus.service.QuizService;
import com.codecampus.service.RegistrationService;
import com.codecampus.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class LessonController {

    @Autowired private UserService userService;
    @Autowired private RegistrationService registrationService;
    @Autowired private LessonService lessonService;
    @Autowired private QuizService quizService;

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
     * Xử lý nút "Vào học" (chỉ có courseId)
     * Tự động tìm bài học đầu tiên và chuyển hướng
     */
    @GetMapping("/learning/{courseId}")
    public String redirectToFirstLesson(@PathVariable("courseId") Integer courseId,
                                        Authentication authentication,
                                        RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";

        // 1. Kiểm tra bảo mật (đã mua COMPLETED chưa)
        boolean hasAccess = registrationService.hasUserRegistered(currentUser.getId(), courseId);

        if (!hasAccess) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền truy cập khóa học này.");
            return "redirect:/my-courses";
        }

        try {
            // 2. Tìm bài học đầu tiên (từ LessonService)
            Lesson firstLesson = lessonService.getFirstLesson(courseId);

            // 3. Chuyển hướng (Redirect) đến URL đầy đủ
            return "redirect:/learning/" + courseId + "/" + firstLesson.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Khóa học này chưa có bài học.");
            return "redirect:/my-courses";
        }
    }

    /**
     * Xử lý hiển thị một bài học cụ thể (Lesson, Quiz, hoặc Lab)
     */
    @GetMapping("/learning/{courseId}/{lessonId}")
    public String showLessonPage(@PathVariable("courseId") Integer courseId,
                                 @PathVariable("lessonId") Integer lessonId,
                                 Model model,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";

        // 1. KIỂM TRA BẢO MẬT
        boolean hasAccess = registrationService.hasUserRegistered(currentUser.getId(), courseId);
        if (!hasAccess) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền truy cập khóa học này.");
            return "redirect:/my-courses";
        }

        try {
            // 2. LẤY DỮ LIỆU (Chỉ lấy 1 lần)
            Lesson currentLesson = lessonService.getLessonById(lessonId);
            List<Lesson> allLessons = lessonService.getLessonsByCourseId(courseId);

            // 3. ĐẨY DỮ LIỆU CHUNG (Cho Sidebar)
            model.addAttribute("currentLesson", currentLesson);
            model.addAttribute("allLessons", allLessons);
            model.addAttribute("course", currentLesson.getCourse());

            // 4. LOGIC CHIA CỔNG (ROUTING)
            String lessonType = currentLesson.getLessonType() != null ? currentLesson.getLessonType().getName() : "Text";

            // Case 1: Nếu là Quiz
            if (("Quiz".equalsIgnoreCase(lessonType) || "Test".equalsIgnoreCase(lessonType))
                    && currentLesson.getQuiz() != null) {

                Quiz quiz = currentLesson.getQuiz();

                Optional<QuizAttempt> lastAttemptOpt = quizService.getLastAttempt(currentUser.getId(), quiz.getId());
                lastAttemptOpt.ifPresent(attempt -> model.addAttribute("lastAttempt", attempt));

                // Sửa lỗi: Check null cho questions
                long totalQuestions = (quiz.getQuestions() != null) ? quiz.getQuestions().size() : 0;

                model.addAttribute("quiz", quiz);
                model.addAttribute("totalQuestions", totalQuestions);

                return "quiz-view"; // Trả về file quiz-view.html

                // Case 2: Nếu là Lab
            } else if ("Lab".equalsIgnoreCase(lessonType) && currentLesson.getLab() != null) {

                // Chuyển hướng sang LabController
                return "redirect:/learning/lab/" + currentLesson.getLab().getId();

                // Case 3: Là Video hoặc Text
            } else {

                // (Không cần lấy lại data)
                // Chỉ cần tính toán Prev/Next
                Integer currentIndex = null;
                for (int i = 0; i < allLessons.size(); i++) {
                    if (allLessons.get(i).getId().equals(lessonId)) {
                        currentIndex = i;
                        break;
                    }
                }

                if (currentIndex != null) {
                    if (currentIndex > 0) {
                        model.addAttribute("prevLesson", allLessons.get(currentIndex - 1));
                    }
                    if (currentIndex < allLessons.size() - 1) {
                        model.addAttribute("nextLesson", allLessons.get(currentIndex + 1));
                    }
                }

                return "lesson-view"; // Trả về file lesson-view.html
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/my-courses";
        }
    }
}