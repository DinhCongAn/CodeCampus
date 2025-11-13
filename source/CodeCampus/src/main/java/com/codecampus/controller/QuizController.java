// src/main/java/com/codecampus/controller/QuizController.java
package com.codecampus.controller;

import com.codecampus.entity.*;
import com.codecampus.repository.QuizAttemptAnswerRepository;
import com.codecampus.repository.QuizAttemptRepository;
import com.codecampus.service.LessonService;
import com.codecampus.service.QuizService;
import com.codecampus.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // BỔ SUNG

import java.util.List;

@Controller
@RequestMapping("/quiz") // Tất cả URL quiz
public class QuizController {

    // BỔ SUNG: Tiêm UserService
    @Autowired
    private UserService userService;

    @Autowired private QuizService quizService;
    @Autowired private LessonService lessonService;
    @Autowired private QuizAttemptRepository quizAttemptRepository;
    @Autowired private QuizAttemptAnswerRepository quizAttemptAnswerRepository;

    // --- BỔ SUNG: Helper lấy User (giống các Controller khác) ---
    private User getCurrentUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            // Văng lỗi nếu hàm yêu cầu xác thực
            throw new RuntimeException("Người dùng chưa được xác thực.");
        }
        try {
            return userService.findUserByEmail(auth.getName());
        } catch (Exception e) {
            throw new RuntimeException("Không tìm thấy thông tin người dùng.", e);
        }
    }
    // ==========================================================

    /**
     * (Màn 16) Xử lý nút "Bắt đầu làm bài"
     */
    @PostMapping("/start")
    public String startQuiz(@RequestParam("quizId") Integer quizId,
                            Authentication auth,
                            RedirectAttributes redirectAttributes) { // BỔ SUNG
        try {
            User user = getCurrentUser(auth);
            Quiz quiz = quizService.getQuizWithQuestions(quizId);
            QuizAttempt attempt = quizService.startQuiz(user, quiz);

            // Chuyển đến câu hỏi đầu tiên (index = 0)
            return "redirect:/quiz/take/" + attempt.getId() + "/question/0";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            // (Chuyển hướng về trang khóa học/my-courses nếu cần)
            return "redirect:/my-courses";
        }
    }

    /**
     * (Màn 17) Hiển thị màn hình Làm bài
     */
    @GetMapping("/take/{attemptId}/question/{questionIndex}")
    public String showQuizQuestion(@PathVariable Integer attemptId,
                                   @PathVariable Integer questionIndex,
                                   Model model,
                                   Authentication auth,
                                   RedirectAttributes redirectAttributes) { // BỔ SUNG
        try {
            User user = getCurrentUser(auth);

            // Hàm này (từ QuizService) đã kiểm tra bảo mật (user có phải chủ)
            // VÀ kiểm tra status = 'in_progress'
            QuizAttempt attempt = quizService.getAttemptForTaking(attemptId, user.getId());

            List<Question> questions = attempt.getQuiz().getQuestions();

            // Kiểm tra chỉ số (index) hợp lệ
            if (questionIndex < 0 || questionIndex >= questions.size()) {
                throw new RuntimeException("Câu hỏi không hợp lệ.");
            }

            Question currentQuestion = questions.get(questionIndex);

            model.addAttribute("attempt", attempt);
            model.addAttribute("question", currentQuestion);
            model.addAttribute("questionIndex", questionIndex);
            model.addAttribute("totalQuestions", questions.size());

            return "quiz-take"; // (Màn 17)

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi tải bài làm: " + e.getMessage());
            return "redirect:/my-courses";
        }
    }

    /**
     * (Màn 18) Hiển thị màn hình Review
     * (ĐÃ SỬA LỖI BẢO MẬT)
     */
    @GetMapping("/review/{attemptId}")
    public String showQuizReview(@PathVariable Integer attemptId, Model model, Authentication auth, RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser(auth);
            QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bài làm."));

            // (Kiểm tra bảo mật)
            if (!attempt.getUser().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền xem kết quả này.");
                return "redirect:/my-courses";
            }

            // ===== SỬA LỖI Ở ĐÂY =====
            // Sửa tên hàm: findByAttemptId -> findByAttempt_Id
            List<QuizAttemptAnswer> userAnswers = quizAttemptAnswerRepository.findByAttempt_Id(attemptId);
            // ========================

            model.addAttribute("attempt", attempt);
            model.addAttribute("userAnswers", userAnswers);
            model.addAttribute("quiz", attempt.getQuiz());

            // (Thêm 2 dòng này để sửa lỗi 'course.id' null ở tin nhắn trước)
            model.addAttribute("course", attempt.getQuiz().getCourse());
            model.addAttribute("currentLesson", attempt.getQuiz().getLesson());

            return "quiz-review"; // (Màn 18)

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/my-courses";
        }
    }
}