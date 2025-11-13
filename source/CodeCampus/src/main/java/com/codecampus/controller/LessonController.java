package com.codecampus.controller;

import com.codecampus.entity.Course;
import com.codecampus.entity.Lesson;
import com.codecampus.entity.User;
import com.codecampus.service.LessonService;
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

@Controller
public class LessonController {

    @Autowired private UserService userService;
    @Autowired private RegistrationService registrationService;
    @Autowired private LessonService lessonService;

    // Helper lấy User (giống các controller khác)
    private User getCurrentUser(Authentication auth) {
        if (auth == null) return null;
        return userService.findUserByEmail(auth.getName());
    }

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
            // 2. Tìm bài học đầu tiên
            Lesson firstLesson = lessonService.getFirstLesson(courseId);

            // 3. Chuyển hướng (Redirect) đến URL đầy đủ
            return "redirect:/learning/" + courseId + "/" + firstLesson.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/my-courses";
        }
    }
    /**
     * Xử lý hiển thị một bài học cụ thể
     */
    @GetMapping("/learning/{courseId}/{lessonId}")
    public String showLessonPage(@PathVariable("courseId") Integer courseId,
                                 @PathVariable("lessonId") Integer lessonId,
                                 Model model,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";

        // 1. KIỂM TRA BẢO MẬT: User đã mua khóa học này (COMPLETED) chưa?
        boolean hasAccess = registrationService.hasUserRegistered(currentUser.getId(), courseId);

        if (!hasAccess) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền truy cập khóa học này.");
            return "redirect:/my-courses";
        }

        try {
            // 2. Lấy dữ liệu bài học hiện tại
            Lesson currentLesson = lessonService.getLessonById(lessonId);

            // 3. Lấy TẤT CẢ bài học (cho sidebar điều hướng)
            List<Lesson> allLessons = lessonService.getLessonsByCourseId(courseId);

            // 4. (Bonus) Tìm bài học Trước (Prev) và Sau (Next)
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

            // 5. Đẩy dữ liệu ra view
            model.addAttribute("currentLesson", currentLesson);
            model.addAttribute("allLessons", allLessons);
            model.addAttribute("course", currentLesson.getCourse()); // Gửi thông tin khóa học (tên,...)

            return "lesson-view"; // Trả về file lesson-view.html

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy bài học.");
            return "redirect:/my-courses";
        }
    }
}