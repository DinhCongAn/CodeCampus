package com.codecampus.controller;

import com.codecampus.entity.Quiz;
import com.codecampus.entity.QuizAttempt;
import com.codecampus.entity.User;
import com.codecampus.service.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/learning/quiz")
public class QuizLearningController {

    private final QuizService quizService;
    private final QuizAttemptService quizAttemptService;
    private final MyCourseService myCourseService;
    private final UserService userService;
    private final AiLearningService aiLearningService;

    @Autowired
    public QuizLearningController(QuizService quizService,
                                  QuizAttemptService quizAttemptService,
                                  MyCourseService myCourseService,
                                  UserService userService,
                                  AiLearningService aiLearningService) {
        this.quizService = quizService;
        this.quizAttemptService = quizAttemptService;
        this.myCourseService = myCourseService;
        this.userService = userService;
        this.aiLearningService = aiLearningService;
    }

    /**
     * Xử lý MH-16: Hiển thị trang giới thiệu/chờ của Quiz.
     */
    @GetMapping("/{quizId}")
    public String getQuizIntroPage(@PathVariable("quizId") Integer quizId,
                                   @RequestParam(required = false) Long lessonId, // Lấy lessonId từ URL
                                   Model model,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {

        if (principal == null) return "redirect:/login";
        User currentUser = userService.findUserByEmail(principal.getName());
        Quiz quiz = quizService.findQuizById(quizId);

        // Kiểm tra bảo mật: User đã đăng ký khóa học này chưa?
        Integer courseId = quiz.getCourse().getId();
        if (!myCourseService.isUserEnrolled(currentUser.getId(), courseId)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập bài quiz này.");
            return "redirect:/my-courses";
        }

        // 1. Lấy lịch sử làm bài (Chức năng DB)
        List<QuizAttempt> pastAttempts = quizAttemptService.findAttemptsByUserAndQuiz(
                currentUser.getId(),
                quizId
        );

        // 2. Lấy gợi ý AI (Tích hợp AI)
//        String aiPrepTips = aiLearningService.getQuizPreparationTips(quizId);

        // 3. Gửi dữ liệu ra View
        model.addAttribute("quiz", quiz);
        model.addAttribute("pastAttempts", pastAttempts);
//        model.addAttribute("aiPrepTips", aiPrepTips); // Gửi gợi ý của AI
        model.addAttribute("lessonId", lessonId); // Để cho nút "Quay lại"

        return "learning/quiz-intro"; // Template mới cho MH-16
    }

    /**
     * Xử lý khi người dùng nhấn "Bắt đầu làm bài"
     */
    @PostMapping("/start")
    public String startQuizAttempt(@RequestParam("quizId") Integer quizId, // Lấy từ form
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {

        if (principal == null) return "redirect:/login";
        User currentUser = userService.findUserByEmail(principal.getName());

        try {
            // Service này sẽ tạo bản ghi 'quiz_attempts' mới
            QuizAttempt newAttempt = quizAttemptService.createNewAttempt(
                    currentUser.getId(),
                    quizId
            );

            // Chuyển hướng đến MH-17 (Màn hình Làm Quiz)
            return "redirect:/learning/attempt/" + newAttempt.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể bắt đầu bài làm. Vui lòng thử lại.");
            return "redirect:/learning/quiz/" + quizId;
        }
    }

    // === THÊM API MỚI CHO AI PREP TIPS ===
    /**
     * API: Lấy gợi ý ôn tập (chỉ gọi khi user bấm nút)
     */
    @GetMapping("/api/prep-tips/{quizId}")
    @ResponseBody // <-- Trả về JSON
    public ResponseEntity<Map<String, String>> getAiPrepTips(@PathVariable Integer quizId, Principal principal) {

        // (Bảo mật)
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Chưa đăng nhập"));
        }
        User currentUser = userService.findUserByEmail(principal.getName());
        Quiz quiz = quizService.findQuizById(quizId);
        if (!myCourseService.isUserEnrolled(currentUser.getId(), quiz.getCourse().getId())) {
            return ResponseEntity.status(403).body(Map.of("error", "Không có quyền"));
        }

        // Gọi AI Service
        String tips = aiLearningService.getQuizPreparationTips(quizId);
        return ResponseEntity.ok(Map.of("tips", tips));
    }
}