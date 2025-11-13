// src/main/java/com/codecampus/service/AsyncLabGradingService.java
package com.codecampus.service;

import com.codecampus.entity.LabAttempt;
import com.codecampus.repository.LabAttemptRepository;
// Bổ sung các import cần thiết
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // BỔ SUNG

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class AsyncLabGradingService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncLabGradingService.class);

    @Autowired
    private LabAttemptRepository labAttemptRepository;

    @Autowired
    private AIService aiService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * SỬA LẠI: Hàm này giờ nhận ID và String,
     * không nhận đối tượng Entity 'attempt'
     */
    @Async // Rất quan trọng!
    @Transactional // BỔ SUNG: Cần @Transactional để lưu (save)
    public void gradeLabAttempt(Integer attemptId, String criteria, String code) {
        logger.info("Bắt đầu chấm điểm (Async) cho Lượt làm (Attempt) ID: {}", attemptId);

        // BỔ SUNG: Tải lại (re-fetch) attempt TRONG LUỒNG NÀY
        LabAttempt attempt = labAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Async: Không tìm thấy Lượt làm bài " + attemptId));

        try {
            // (Không cần lấy criteria và code nữa, vì đã được truyền vào)

            // 1. Gọi AI để chấm (có thể mất 30 giây)
            String jsonResult = aiService.gradeLab(criteria, code);
            logger.debug("AI trả về JSON cho Lượt {}: {}", attempt.getId(), jsonResult);

            // 2. Parse JSON
            BigDecimal score;
            String feedback;

            try {
                JsonNode rootNode = objectMapper.readTree(jsonResult);
                score = new BigDecimal(rootNode.get("score").asDouble());
                feedback = rootNode.get("feedback").asText();

            } catch (Exception e) {
                logger.error("Lỗi parse JSON từ AI cho Lượt {}: {}", attempt.getId(), e.getMessage());
                score = BigDecimal.ZERO;
                feedback = "Lỗi: Hệ thống AI không thể chấm bài của bạn. (Lỗi parse JSON)";
            }

            // 3. Cập nhật kết quả (khớp với DBScript)
            attempt.setAiGrade(score);
            attempt.setAiFeedback(feedback);
            attempt.setStatus("graded"); // Đổi trạng thái: Đã chấm xong
            attempt.setCompletedAt(LocalDateTime.now());

            labAttemptRepository.save(attempt);
            logger.info("Hoàn tất chấm điểm (Async) cho Lượt ID: {}. Điểm: {}", attempt.getId(), score);

        } catch (Exception e) {
            logger.error("Lỗi nghiêm trọng khi chấm điểm cho Lượt {}: {}", attempt.getId(), e.getMessage());

            attempt.setStatus("grading_failed");
            attempt.setAiFeedback("Hệ thống chấm điểm AI gặp lỗi: " + e.getMessage());
            labAttemptRepository.save(attempt);
        }
    }
}