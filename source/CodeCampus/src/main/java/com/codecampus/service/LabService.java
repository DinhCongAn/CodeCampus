package com.codecampus.service;

import com.codecampus.entity.*;
import com.codecampus.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class LabService {

    private static final Logger logger = LoggerFactory.getLogger(LabService.class);

    // Tiêm các Repositories và Services cần thiết
    @Autowired private LabRepository labRepository;
    @Autowired private LabAttemptRepository labAttemptRepository;
    @Autowired private LabAiInteractionRepository interactionRepository;
    @Autowired private AiLearningService aiService;
    @Autowired private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    // Dùng ExecutorService để mô phỏng tác vụ chấm điểm BẤT ĐỒNG BỘ
    private final ExecutorService gradingExecutor = Executors.newSingleThreadExecutor();


    /**
     * 1. Bắt đầu Lab (Tìm lượt "in_progress" cũ, nếu không có thì tạo mới)
     */
    @Transactional
    public LabAttempt startOrGetLabAttempt(Integer labId, User user) {

        Optional<LabAttempt> existingAttempt = labAttemptRepository
                .findFirstByLabIdAndUserIdAndStatus(labId, user.getId(), "in_progress");

        if (existingAttempt.isPresent()) {
            return existingAttempt.get();
        }

        Lab lab = labRepository.findById(labId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Lab"));

        LabAttempt newAttempt = new LabAttempt();
        newAttempt.setUser(user);
        newAttempt.setLab(lab);
        newAttempt.setStatus("in_progress");
        newAttempt.setStartedAt(LocalDateTime.now());

        return labAttemptRepository.save(newAttempt);
    }

    /**
     * 2. Nộp bài (chuyển sang chấm bất đồng bộ)
     */
    @Transactional
    public void submitLab(Integer attemptId, String userCode, Integer userId) {

        LabAttempt attempt = labAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Lượt làm bài"));

        // Kiểm tra bảo mật (user này có phải chủ nhân của attempt không)
        if (!attempt.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền nộp bài cho lượt làm này.");
        }

        // Cập nhật nội dung và trạng thái
        attempt.setSubmittedContent(userCode);
        attempt.setStatus("grading");
        LabAttempt savedAttempt = labAttemptRepository.save(attempt);

        // Kích hoạt chấm điểm BẤT ĐỒNG BỘ
        // Chú ý: Cần đảm bảo attempt này có thể được tải lại trong thread mới (lý tưởng là @Async)
        gradingExecutor.submit(() -> gradeLabAttempt(savedAttempt.getId()));
    }

    /**
     * Chức năng CHẤM ĐIỂM THẬT (Chạy trong thread nền)
     */
    public void gradeLabAttempt(Integer attemptId) {
        // Lấy lại attempt trong transaction mới (vì nó chạy bất đồng bộ)
        LabAttempt attempt = labAttemptRepository.findById(attemptId).orElse(null);

        if (attempt == null) {
            logger.error("LỖI CHẤM ĐIỂM: Không tìm thấy attempt ID {}", attemptId);
            return;
        }

        try {
            Lab lab = attempt.getLab();
            String labCriteria = lab.getEvaluationCriteria();
            String userCode = attempt.getSubmittedContent();

            // 1. Gọi AI Service (Hàm này trả về JSON string)
            String gradingJson = aiService.gradeLab(labCriteria, userCode);

            // 2. Xử lý kết quả JSON
            Map<String, Object> result = objectMapper.readValue(gradingJson, new TypeReference<Map<String, Object>>(){});

            // Xử lý điểm (BigDecimal)
            BigDecimal score;
            Object scoreObj = result.get("score");
            if (scoreObj instanceof Number) {
                score = BigDecimal.valueOf(((Number) scoreObj).doubleValue());
            } else {
                score = new BigDecimal(scoreObj.toString());
            }

            String feedback = result.get("feedback").toString();

            // 3. Cập nhật trạng thái
            // Cần chạy trong một @Transactional riêng nếu không dùng @Async
            updateLabAttemptStatus(attempt.getId(), score, feedback, "graded");

        } catch (Exception e) {
            logger.error("Lỗi chấm điểm LAB cho attempt {}: {}", attempt.getId(), e.getMessage(), e);
            updateLabAttemptStatus(attempt.getId(), BigDecimal.ZERO, "Hệ thống chấm AI gặp lỗi: " + e.getMessage(), "grading_failed");
        }
    }

    /**
     * Helper: Cập nhật trạng thái Lab trong Transaction riêng (an toàn cho async)
     */
    @Transactional
    public void updateLabAttemptStatus(Integer attemptId, BigDecimal score, String feedback, String status) {
        LabAttempt attempt = labAttemptRepository.findById(attemptId).orElse(null);
        if (attempt != null) {
            attempt.setAiGrade(score.setScale(2, RoundingMode.HALF_UP));
            attempt.setAiFeedback(feedback);
            attempt.setStatus(status);
            attempt.setCompletedAt(LocalDateTime.now());
            labAttemptRepository.save(attempt);
        }
    }


    /**
     * 3. AI Hỗ trợ (Chat)
     */
    @Transactional
    public String getLabHelp(Integer attemptId, Integer userId, String userQuestion) {

        LabAttempt attempt = labAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Lượt làm bài"));

        if (!attempt.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền chat cho lượt làm này.");
        }

        String labContext = attempt.getLab().getDescription();

        String aiResponse = aiService.getLabHelp(labContext, userQuestion);

        // Lưu lại lịch sử chat
        LabAiInteraction interaction = new LabAiInteraction();
        interaction.setAttempt(attempt);
        interaction.setUserPrompt(userQuestion);
        interaction.setAiResponse(aiResponse);
        interaction.setTimestamp(LocalDateTime.now());
        interactionRepository.save(interaction);

        return aiResponse;
    }

    /**
     * 4. (MỚI) Lấy trạng thái Lab (cho Polling)
     */
    @Transactional(readOnly = true)
    public LabAttempt getLabAttemptStatus(Integer attemptId, Integer userId) {
        // Cần tải đầy đủ để trả về LabAttemptController
        return labAttemptRepository.findByIdAndUserId(attemptId, userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hoặc không có quyền truy cập lượt làm bài này."));
    }
    // =========================================================
    // === PHẦN SỬA ĐỔI LOGIC TẢI VÀ TẠO ATTEMPT BÀI LAB ===
    // =========================================================

    /**
     * 1. Lấy lượt làm Lab mới nhất của User (Dùng cho việc tải trang)
     * Yêu cầu LessonRepository có: findFirstByLabIdAndUserIdOrderByStartedAtDesc
     */
    @Transactional(readOnly = true)
    public Optional<LabAttempt> getLatestLabAttempt(Integer labId, Integer userId) {
        // Hàm này sẽ tìm lượt làm gần nhất, bất kể status là 'graded', 'grading', hay 'in_progress'
        return labAttemptRepository.findFirstByLabIdAndUserIdOrderByStartedAtDesc(labId, userId);
    }

    /**
     * 2. Tạo một lượt làm Lab MỚI hoàn toàn (Dùng cho nút "Làm Lại")
     */
    @Transactional
    public LabAttempt createNewLabAttempt(Integer labId, User user) {
        Lab lab = labRepository.findById(labId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Lab"));

        LabAttempt newAttempt = new LabAttempt();
        newAttempt.setUser(user);
        newAttempt.setLab(lab);
        newAttempt.setStatus("in_progress"); // Trạng thái ban đầu
        newAttempt.setStartedAt(LocalDateTime.now());
        // RẤT QUAN TRỌNG: Đảm bảo code ban đầu là NULL hoặc chuỗi rỗng
        newAttempt.setSubmittedContent(null);
        newAttempt.setAiGrade(null);
        newAttempt.setAiFeedback(null);

        return labAttemptRepository.save(newAttempt);
    }
}
