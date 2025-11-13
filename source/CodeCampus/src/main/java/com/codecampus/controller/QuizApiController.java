// src/main/java/com/codecampus/controller/QuizApiController.java
package com.codecampus.controller;

import com.codecampus.entity.QuizAttempt;
import com.codecampus.entity.User;
import com.codecampus.service.QuizService;
import com.codecampus.service.UserService; // BỔ SUNG
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // BỔ SUNG
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/quiz")
public class QuizApiController {

    @Autowired
    private QuizService quizService;

    @Autowired
    private UserService userService; // BỔ SUNG

    // BỔ SUNG: Hàm helper để lấy User
    private User getCurrentUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Người dùng chưa được xác thực.");
        }
        return userService.findUserByEmail(auth.getName());
    }

    // (DTO để nhận câu trả lời)
    @Data
    static class AnswerPayload {
        private Integer attemptId;
        private Integer questionId;
        private Integer answerOptionId;
        private Integer questionIndex;
    }

    @PostMapping("/submit-answer")
    public ResponseEntity<?> submitAnswer(@RequestBody AnswerPayload payload, Authentication auth) {
        try {
            User user = getCurrentUser(auth);

            // 1. Lấy Attempt (Hàm này giờ đã JOIN FETCH q.questions)
            QuizAttempt attempt = quizService.getAttemptForTaking(payload.getAttemptId(), user.getId());

            // 2. Lưu câu trả lời
            quizService.saveAnswer(payload.getAttemptId(), payload.getQuestionId(), payload.getAnswerOptionId());

            // 3. Tính toán câu tiếp theo
            Integer nextIndex = payload.getQuestionIndex() + 1;

            // 4. Lấy totalQuestions (Giờ đã an toàn, không lỗi)
            int totalQuestions = attempt.getQuiz().getQuestions().size();

            String nextUrl;
            if (nextIndex >= totalQuestions) { // Dùng >=
                // Hết câu, chuyển đến API nộp bài (Finish)
                nextUrl = "/api/quiz/finish";
            } else {
                // Còn câu, chuyển đến trang câu tiếp
                nextUrl = "/quiz/take/" + payload.getAttemptId() + "/question/" + nextIndex;
            }

            // Trả về: ví dụ {"nextUrl": "/quiz/take/8/question/1"}
            return ResponseEntity.ok(Map.of("nextUrl", nextUrl));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * (Màn 17) Xử lý nút "Nộp bài" (ĐÃ SỬA LỖI)
     */
    @PostMapping("/finish")
    public ResponseEntity<?> finishQuiz(@RequestParam("attemptId") Integer attemptId, Authentication auth) {
        try {
            User user = getCurrentUser(auth);

            // 1. Chấm điểm
            QuizAttempt finishedAttempt = quizService.gradeQuiz(attemptId, user.getId());

            // 2. Lấy ID (Đảm bảo Lesson và Course được fetch cùng Quiz)
            // (Service của bạn phải đảm bảo 'getQuiz()' không bị Lazy)
            Integer lessonId = finishedAttempt.getQuiz().getLesson().getId();
            Integer courseId = finishedAttempt.getQuiz().getCourse().getId();

            if(lessonId == null || courseId == null) {
                throw new RuntimeException("Lỗi cấu hình: Quiz chưa được gán vào Lesson hoặc Course.");
            }

            String returnUrl = "/learning/" + courseId + "/" + lessonId;
            return ResponseEntity.ok(Map.of("returnUrl", returnUrl));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * (Màn 17) Xử lý nút "Gợi ý" (ĐÃ SỬA LỖI)
     */
    @PostMapping("/get-hint")
    public ResponseEntity<?> getHint(@RequestParam Integer attemptId,
                                     @RequestParam Integer questionId,
                                     Authentication auth) {
        try {
            // 1. Kiểm tra bảo mật
            User user = getCurrentUser(auth);
            quizService.getAttemptForTaking(attemptId, user.getId()); // Chỉ gọi để check security

            // 2. Lấy gợi ý
            String hint = quizService.getHint(attemptId, questionId);
            return ResponseEntity.ok(Map.of("hint", hint));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
}