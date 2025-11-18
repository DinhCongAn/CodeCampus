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

    @GetMapping("/api/analysis/{attemptId}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getAnalysis(@PathVariable Integer attemptId, Principal principal) {
        User currentUser = userService.findUserByEmail(principal.getName());

        // === SỬA Ở ĐÂY ===
        // Gọi hàm mới 'getFullQuizAnalysis'
        String analysis = aiService.getFullQuizAnalysis(attemptId, currentUser.getId());

        return ResponseEntity.ok(Map.of("analysis", analysis));
    }}