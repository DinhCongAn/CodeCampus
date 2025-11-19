package com.codecampus.repository;

import com.codecampus.entity.LabAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LabAttemptRepository extends JpaRepository<LabAttempt, Integer> {
    // Hàm mới: Tìm lượt đang làm dở
    Optional<LabAttempt> findFirstByLabIdAndUserIdAndStatus(Integer labId, Integer userId, String status);

    /**
     * Hỗ trợ getLabAttemptStatus (Kiểm tra bảo mật)
     * Tìm một lượt làm bài bằng ID VÀ chủ sở hữu (userId)
     */
    Optional<LabAttempt> findByIdAndUserId(Integer attemptId, Integer userId);
    Optional<LabAttempt> findFirstByLabIdAndUserIdOrderByStartedAtDesc(Integer labId, Integer userId);
}
