package com.codecampus.controller;

import com.codecampus.dto.QuestionDto; // <-- THÊM IMPORT
import com.codecampus.entity.Question; // <-- THÊM IMPORT
import com.codecampus.dto.SaveAnswerRequest;
import com.codecampus.dto.SubmitQuizRequest;
import com.codecampus.entity.QuizAttempt;
import com.codecampus.entity.QuizAttemptAnswer;
import com.codecampus.entity.User;
import com.codecampus.repository.QuestionRepository; // <-- THÊM IMPORT
import com.codecampus.service.AiLearningService;
import com.codecampus.service.QuizAttemptService;
import com.codecampus.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; // <-- THÊM IMPORT

@Controller
@RequestMapping("/learning/attempt")
public class QuizAttemptController {

    @Autowired private QuizAttemptService attemptService;
    @Autowired private UserService userService;
    @Autowired private AiLearningService aiService;
    @Autowired private QuestionRepository questionRepository; // <-- THÊM REPO

    /**
     * Tải trang làm Quiz (MH-17) - ĐÃ SỬA
     */
    @GetMapping("/{attemptId}")
    public String getQuizHandlePage(@PathVariable("attemptId") Integer attemptId,
                                    Model model, Principal principal,
                                    RedirectAttributes redirectAttributes) { // <-- THÊM RedirectAttributes

        User currentUser = userService.findUserByEmail(principal.getName());

        // === SỬA LỖI Ở ĐÂY: Thêm Try-Catch ===
        try {
            // Tải thông tin lần làm bài (hàm này sẽ ném lỗi nếu đã "completed")
            QuizAttempt attempt = attemptService.getLiveAttempt(attemptId, currentUser.getId());

            // (Code bên dưới chỉ chạy nếu bài làm "in_progress")
            List<QuizAttemptAnswer> savedAnswers = attemptService.getSavedAnswers(attemptId);

            Map<Integer, Integer> selectedAnswersMap = savedAnswers.stream()
                    .filter(ans -> ans.getSelectedAnswerOption() != null)
                    .collect(Collectors.toMap(
                            ans -> ans.getQuestion().getId(),
                            ans -> ans.getSelectedAnswerOption().getId()
                    ));

            ObjectMapper objectMapper = new ObjectMapper();
            String questionsJson = "[]";
            String savedAnswersJson = "{}";

            try {
                Integer quizId = attempt.getQuiz().getId();
                List<Question> questionEntities = questionRepository.findQuestionsByQuizIdWithOptions(quizId);
                List<QuestionDto> questionDtos = questionEntities.stream()
                        .map(QuestionDto::fromEntity)
                        .collect(Collectors.toList());

                questionsJson = objectMapper.writeValueAsString(questionDtos);
                savedAnswersJson = objectMapper.writeValueAsString(selectedAnswersMap);

            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            model.addAttribute("attempt", attempt);
            model.addAttribute("questionsJson", questionsJson);
            model.addAttribute("savedAnswersJson", savedAnswersJson);

            return "learning/quiz-handle"; // Trả về trang làm quiz

        } catch (RuntimeException e) {
            // Bắt lỗi "Bài làm đã kết thúc"
            if (e.getMessage() != null && e.getMessage().equals("Bài làm này đã kết thúc.")) {
                // Tự động chuyển hướng đến trang Review (MH-18)
                return "redirect:/learning/review/" + attemptId;
            }

            // Bắt các lỗi khác (vd: không có quyền)
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/my-courses";
        }
        // === KẾT THÚC SỬA LỖI ===
    }

    /**
     * API: Lưu câu trả lời (AJAX) - ĐÃ SỬA
     */
    @PostMapping("/api/save-answer")
    public ResponseEntity<Void> saveAnswer(@RequestBody SaveAnswerRequest request, Principal principal) {
        // Kiểm tra null (nguyên nhân gây lỗi)
        if (request.getAnswerId() == null || request.getQuestionId() == null) {
            // Lỗi này xảy ra do DTO không được tạo đúng ở 'getQuizHandlePage'
            return ResponseEntity.badRequest().build();
        }
        User currentUser = userService.findUserByEmail(principal.getName());
        attemptService.saveAnswer(request, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * API: Lấy gợi ý AI (AJAX)
     */
    @GetMapping("/api/hint/{attemptId}/{questionId}")
    public ResponseEntity<Map<String, String>> getHint(@PathVariable Integer attemptId,
                                                       @PathVariable Integer questionId,
                                                       Principal principal) {
        User currentUser = userService.findUserByEmail(principal.getName());

        attemptService.incrementHintCount(attemptId, currentUser.getId());
        String hint = aiService.getQuizQuestionHint(questionId);

        return ResponseEntity.ok(Map.of("hint", hint));
    }

    /**
     * API: Nộp bài (AJAX)
     */
    @PostMapping("/api/submit")
    public ResponseEntity<Map<String, String>> submitQuiz(@RequestBody SubmitQuizRequest request,
                                                          Principal principal) {
        User currentUser = userService.findUserByEmail(principal.getName());

        QuizAttempt gradedAttempt = attemptService.submitAndGradeQuiz(request.getAttemptId(), currentUser.getId());

        String reviewUrl = "/learning/review/" + gradedAttempt.getId();
        return ResponseEntity.ok(Map.of("reviewUrl", reviewUrl));
    }
}