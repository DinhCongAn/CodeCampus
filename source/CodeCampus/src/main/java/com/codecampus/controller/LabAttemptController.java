package com.codecampus.controller;

import com.codecampus.entity.LabAttempt;
import com.codecampus.entity.User;
import com.codecampus.service.LabService;
import com.codecampus.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api/lab")
public class LabAttemptController {

    @Autowired private LabService labService;
    @Autowired private UserService userService;

    /**
     * API 1: Xử lý chat (AI Hỗ trợ) - POST /api/lab/chat
     */
    @PostMapping("/chat")
    @ResponseBody
    public ResponseEntity<Map<String, String>> chatWithLabAi(@RequestParam Integer attemptId,
                                                             @RequestParam String prompt,
                                                             Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        User currentUser = userService.findUserByEmail(principal.getName());

        try {
            String aiResponse = labService.getLabHelp(attemptId, currentUser.getId(), prompt);
            return ResponseEntity.ok(Map.of("response", aiResponse));

        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * API 2: Xử lý nộp bài Lab - POST /api/lab/submit
     */
    @PostMapping("/submit")
    @ResponseBody
    public ResponseEntity<Map<String, String>> submitLab(@RequestParam Integer attemptId,
                                                         @RequestParam String code,
                                                         Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        User currentUser = userService.findUserByEmail(principal.getName());

        try {
            labService.submitLab(attemptId, code, currentUser.getId());

            // Trả về trạng thái đang chấm để client bắt đầu polling
            return ResponseEntity.ok(Map.of("status", "grading"));

        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage(), "status", "submission_failed"));
        }
    }

    /**
     * API 3: Kiểm tra trạng thái chấm điểm (Polling) - GET /api/lab/status/{attemptId}
     */
    @GetMapping("/status/{attemptId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getGradingStatus(@PathVariable Integer attemptId,
                                                                Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User currentUser = userService.findUserByEmail(principal.getName());

        try {
            LabAttempt attempt = labService.getLabAttemptStatus(attemptId, currentUser.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("status", attempt.getStatus());

            if (attempt.getStatus().equals("graded") || attempt.getStatus().equals("grading_failed")) {
                response.put("score", attempt.getAiGrade());
                response.put("feedback", attempt.getAiFeedback());
            }
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage(), "status", "not_found"));
        }
    }
}