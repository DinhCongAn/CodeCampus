// src/main/java/com/codecampus/service/LabService.java
package com.codecampus.service;

import com.codecampus.entity.Lab; // BỔ SUNG
import com.codecampus.entity.LabAiInteraction;
import com.codecampus.entity.LabAttempt;
import com.codecampus.entity.User;
import com.codecampus.repository.LabAiInteractionRepository;
import com.codecampus.repository.LabAttemptRepository;
import com.codecampus.repository.LabRepository;
import org.springframework.transaction.annotation.Transactional; // SỬA: Dùng @Transactional của Spring
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime; // BỔ SUNG
import java.util.Optional; // BỔ SUNG

@Service
public class LabService {

    @Autowired private LabRepository labRepository;
    @Autowired private LabAttemptRepository labAttemptRepository;
    @Autowired private LabAiInteractionRepository interactionRepository;
    @Autowired private AIService aiService;
    @Autowired private AsyncLabGradingService asyncGradingService;

    /**
     * 1. Bắt đầu Lab
     * (Tìm lượt "in_progress" cũ, nếu không có thì tạo mới)
     */
    @Transactional
    public LabAttempt startOrGetLabAttempt(Integer labId, User user) {

        // 1. Tìm xem có lượt nào đang làm dở (in_progress) không
        // (Bạn cần thêm hàm này vào LabAttemptRepository)
        Optional<LabAttempt> existingAttempt = labAttemptRepository
                .findFirstByLabIdAndUserIdAndStatus(labId, user.getId(), "in_progress");

        if (existingAttempt.isPresent()) {
            // Nếu có, trả về lượt cũ
            return existingAttempt.get();
        }

        // 2. Nếu không có, tạo lượt mới
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

        // (Kiểm tra bảo mật user)
        if (!attempt.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền nộp bài cho lượt làm này.");
        }

        // Cập nhật nội dung và trạng thái
        attempt.setSubmittedContent(userCode);
        attempt.setStatus("grading"); // Đang chấm
        labAttemptRepository.save(attempt);

        // ===== SỬA LẠI CÁCH GỌI =====
        // Lấy các String ra TRƯỚC khi gọi @Async
        String criteria = attempt.getLab().getEvaluationCriteria();

        // Gọi service BẤT ĐỒNG BỘ (@Async) và chỉ truyền ID/String
        asyncGradingService.gradeLabAttempt(attempt.getId(), criteria, userCode);
        // =============================
    }

    /**
     * 3. AI Hỗ trợ (Chat)
     * (Cập nhật: Thêm userId để kiểm tra bảo mật)
     */
    @Transactional
    public String getLabHelp(Integer attemptId, Integer userId, String userQuestion) {

        LabAttempt attempt = labAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Lượt làm bài"));

        // BỔ SUNG: Kiểm tra bảo mật
        if (!attempt.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền chat cho lượt làm này.");
        }

        // (Bạn có thể thêm logic kiểm tra giới hạn chat ở đây nếu muốn)

        // Lấy ngữ cảnh (context) của bài lab
        String labContext = attempt.getLab().getDescription();

        // Gọi AI Service
        String aiResponse = aiService.getLabHelp(labContext, userQuestion);

        // Lưu lại lịch sử chat (khớp với DBScript)
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
     * (Hàm này thực hiện kiểm tra bảo mật)
     */
    @Transactional(readOnly = true)
    public LabAttempt getLabAttemptStatus(Integer attemptId, Integer userId) {
        // (Bạn cần thêm hàm này vào LabAttemptRepository)
        return labAttemptRepository.findByIdAndUserId(attemptId, userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hoặc không có quyền truy cập lượt làm bài này."));
    }
}