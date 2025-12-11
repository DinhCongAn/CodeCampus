package com.codecampus.controller;

import com.codecampus.entity.User;
import com.codecampus.service.AiConsultantService;
import com.codecampus.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiConsultantController {

    private static final Logger logger = LoggerFactory.getLogger(AiConsultantController.class);

    private final AiConsultantService aiService;
    private final UserService userService;

    @Autowired
    public AiConsultantController(AiConsultantService aiService, UserService userService) {
        this.aiService = aiService;
        this.userService = userService;
    }

    @PostMapping("/consult")
    public ResponseEntity<Map<String, String>> consultAi(@RequestBody Map<String, String> request) {
        logger.info("📩 Nhận request chat từ Client: {}", request.get("message"));

        Map<String, String> response = new HashMap<>();

        try {
            String userMessage = request.get("message");

            if (userMessage == null || userMessage.trim().isEmpty()) {
                response.put("reply", "Bạn ơi, bạn chưa nhập câu hỏi nào cả! 🤔");
                return ResponseEntity.badRequest().body(response);
            }

            // 🔐 LẤY USER ĐANG LOGIN (HOẶC NULL KHI GUEST)
            Integer userId = null;
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (principal != null && !"anonymousUser".equals(principal.toString())) {
                String email = principal.toString(); // Spring Security lưu email làm principal
                User u = userService.findUserByEmail(email);
                if (u != null) {
                    userId = u.getId();
                }
            }

            // Gọi service AI
            String aiReply = aiService.getConsultation(userMessage, userId);

            if (aiReply == null) {
                aiReply = "Xin lỗi, hệ thống hơi delay. Bạn thử lại sau nhé! 🛠️";
            }

            response.put("reply", aiReply);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Error in consultAi: ", e);
            response.put("reply", "Hệ thống gặp trục trặc nha bro, thử lại sau 😭🔥");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
