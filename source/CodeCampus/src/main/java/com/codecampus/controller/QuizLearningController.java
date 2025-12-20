package com.codecampus.controller;

import com.codecampus.entity.*;
import com.codecampus.repository.QuestionRepository;
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
    private final LessonService lessonService;
    @Autowired private QuestionRepository questionRepository; // Inject thêm Repo này

    @Autowired
    public QuizLearningController(QuizService quizService,
                                  QuizAttemptService quizAttemptService,
                                  MyCourseService myCourseService,
                                  UserService userService,
                                  LessonService lessonService,
                                  AiLearningService aiLearningService) {
        this.quizService = quizService;
        this.quizAttemptService = quizAttemptService;
        this.myCourseService = myCourseService;
        this.userService = userService;
        this.aiLearningService = aiLearningService;
        this.lessonService = lessonService;
    }


    /**
     * MH-16: Hiển thị trang giới thiệu/chờ của Quiz.
     * Cập nhật: Đẩy đủ dữ liệu Course/Lesson để hiển thị Sidebar.
     */
    /**
     * Màn hình 16: Quiz Intro
     * Logic: Lấy dữ liệu Quiz + Lấy dữ liệu điều hướng (Sidebar, Prev/Next) giống LessonView
     */
    @GetMapping("/{quizId}")
    public String getQuizIntroPage(@PathVariable("quizId") Integer quizId,
                                   @RequestParam(required = false) Integer lessonId, // Nhận lessonId từ redirect (optional)
                                   Model model,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {

        // 1. Kiểm tra đăng nhập
        if (principal == null) return "redirect:/login";
        User currentUser = userService.findUserByEmail(principal.getName());
        if (currentUser == null) return "redirect:/login?error=user_not_found";

        try {
            // 2. Xác định Lesson hiện tại dựa trên QuizId
            // (Logic: Quiz nằm trong 1 Lesson. Ta cần Lesson để biết Course và vị trí Sidebar)
            Lesson currentLesson = lessonService.findLessonByQuizId(quizId);

            // Fallback: Nếu không tìm thấy qua QuizId nhưng có lessonId trên URL
            if (currentLesson == null && lessonId != null) {
                currentLesson = lessonService.getLessonById(lessonId);
            }

            if (currentLesson == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy bài học tương ứng.");
                return "redirect:/my-courses";
            }

            // 3. Lấy Course và kiểm tra quyền truy cập
            Course course = currentLesson.getCourse();
            if (!myCourseService.isUserEnrolled(currentUser.getId(), course.getId())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập khóa học này.");
                return "redirect:/my-courses";
            }

            // 4. Lấy danh sách bài học để vẽ Sidebar & Tính toán Prev/Next
            List<Lesson> allLessons = lessonService.getActiveLessonsByCourseId(course.getId());

            Lesson prevLesson = null;
            Lesson nextLesson = null;
            int currentIndex = -1;
            Long currentLessonId = currentLesson.getId(); // Giả sử ID là Long, nếu Integer thì đổi type

            for (int i = 0; i < allLessons.size(); i++) {
                if (allLessons.get(i).getId().equals(currentLessonId)) {
                    currentIndex = i;
                    break;
                }
            }

            if (currentIndex > 0) {
                prevLesson = allLessons.get(currentIndex - 1);
            }
            if (currentIndex != -1 && currentIndex < allLessons.size() - 1) {
                nextLesson = allLessons.get(currentIndex + 1);
            }

            // 5. Lấy dữ liệu riêng của màn hình Quiz (Lịch sử làm bài, Info Quiz)
            // Lưu ý: currentLesson.getQuiz() có thể null nếu fetch lười (Lazy), nên gọi service cho chắc
            Quiz quiz = (currentLesson.getQuiz() != null) ? currentLesson.getQuiz() : quizService.findQuizById(quizId);

            int totalActiveQuestions = questionRepository.countActiveQuestionsByQuizId(quizId);
            List<QuizAttempt> pastAttempts = quizAttemptService.findAttemptsByUserAndQuiz(currentUser.getId(), quizId);

            // 6. Đẩy dữ liệu ra View (Model Attributes)

            // --- Nhóm dữ liệu chung (Layout/Sidebar/Nav) ---
            model.addAttribute("totalActiveQuestions", totalActiveQuestions);
            model.addAttribute("currentLesson", currentLesson);
            model.addAttribute("course", course);
            model.addAttribute("allLessons", allLessons);
            model.addAttribute("prevLesson", prevLesson);
            model.addAttribute("nextLesson", nextLesson);
            model.addAttribute("currentUserId", currentUser.getId());

            // --- Nhóm dữ liệu riêng (Quiz Content) ---
            model.addAttribute("quiz", quiz);
            model.addAttribute("pastAttempts", pastAttempts);

            return "learning/quiz-intro"; // Trả về màn hình 16 đã thiết kế lại

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống: " + e.getMessage());
            return "redirect:/my-courses";
        }
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