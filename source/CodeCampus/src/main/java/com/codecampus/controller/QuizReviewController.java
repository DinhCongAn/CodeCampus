package com.codecampus.controller;

import com.codecampus.dto.QuizReviewDto;
import com.codecampus.entity.User;
import com.codecampus.service.AiLearningService;
import com.codecampus.service.QuizAttemptService;
import com.codecampus.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@Controller
@RequestMapping("/learning/review")
public class QuizReviewController {

    @Autowired private QuizAttemptService attemptService;
    @Autowired private UserService userService;
    @Autowired private AiLearningService aiService;

    /**
     * Tải trang Review Quiz (MH-18)
     */
    @GetMapping("/{attemptId}")
    public String getReviewPage(@PathVariable("attemptId") Integer attemptId,
                                Model model, Principal principal) {

        User currentUser = userService.findUserByEmail(principal.getName());

        try {
            // Đã truyền userId thật vào đây
            QuizReviewDto reviewData = attemptService.getQuizReview(attemptId, currentUser.getId());
            model.addAttribute("review", reviewData);
            return "learning/quiz-review";

        } catch (Exception e) {
            // (Nếu không có quyền, hoặc attempt không tồn tại, sẽ bị văng ra đây)
            return "redirect:/my-courses";
        }
    }

    // --- API CHO TÍCH HỢP AI ---

    /**
     * API (MH-18): AI Phân tích hiệu suất (SỬA LẠI)
     */
    @GetMapping("/api/analysis/{attemptId}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getAnalysis(@PathVariable Integer attemptId, Principal principal) {
        // === SỬA Ở ĐÂY ===
        // Lấy user và truyền userId vào service
        User currentUser = userService.findUserByEmail(principal.getName());
        String analysis = aiService.getPerformanceAnalysis(attemptId, currentUser.getId());

        return ResponseEntity.ok(Map.of("analysis", analysis));
    }

    /**
     * API (MH-18): AI Giải thích lỗi sai (SỬA LẠI)
     */
    @GetMapping("/api/explain-mistake/{attemptId}/{questionId}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getExplanation(@PathVariable Integer attemptId,
                                                              @PathVariable Integer questionId,
                                                              Principal principal) {
        // === SỬA Ở ĐÂY ===
        // Lấy user và truyền userId vào service
        User currentUser = userService.findUserByEmail(principal.getName());
        String explanation = aiService.getMistakeExplanation(attemptId, questionId, currentUser.getId());

        return ResponseEntity.ok(Map.of("explanation", explanation));
    }
}