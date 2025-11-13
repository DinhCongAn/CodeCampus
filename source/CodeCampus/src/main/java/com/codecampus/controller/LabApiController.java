// src/main/java/com/codecampus/controller/LabApiController.java
package com.codecampus.controller;

import com.codecampus.entity.LabAttempt;
import com.codecampus.entity.User;
import com.codecampus.service.LabService;
import com.codecampus.service.UserService; // BỔ SUNG
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/lab")
public class LabApiController {

    @Autowired private LabService labService;
    @Autowired private UserService userService; // BỔ SUNG

    // BỔ SUNG: Hàm helper để lấy User
    private User getCurrentUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Người dùng chưa được xác thực.");
        }
        return userService.findUserByEmail(auth.getName());
    }

    // 1. API Nộp bài
    @PostMapping("/submit")
    public ResponseEntity<?> submitLab(@RequestParam Integer attemptId, @RequestParam String code, Authentication auth) {
        User user = getCurrentUser(auth);
        labService.submitLab(attemptId, code, user.getId());
        return ResponseEntity.ok(Map.of("status", "grading"));
    }

    // 2. API Chat
    @PostMapping("/chat")
    public ResponseEntity<?> chatWithAi(@RequestParam Integer attemptId, @RequestParam String prompt, Authentication auth) {
        User user = getCurrentUser(auth);
        String response = labService.getLabHelp(attemptId, user.getId(), prompt);
        return ResponseEntity.ok(Map.of("response", response));
    }

    // 3. API Kiểm tra trạng thái (cho Polling)
    @GetMapping("/status/{attemptId}")
    public ResponseEntity<?> getLabStatus(@PathVariable Integer attemptId, Authentication auth) {
        User user = getCurrentUser(auth);
        // SỬA: Gọi Service (đã có kiểm tra bảo mật)
        LabAttempt attempt = labService.getLabAttemptStatus(attemptId, user.getId());

        // Luôn trả về đầy đủ cấu trúc
        return ResponseEntity.ok(Map.of(
                "status", attempt.getStatus(),
                "score", attempt.getAiGrade(),
                "feedback", attempt.getAiFeedback()
        ));
    }
}