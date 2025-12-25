package com.codecampus.repository;

import com.codecampus.entity.LabAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository quản lý các lượt làm bài thực hành (Lab Attempt).
 * Hỗ trợ theo dõi tiến độ thực hành và kiểm tra quyền sở hữu bài làm.
 */
@Repository
public interface LabAttemptRepository extends JpaRepository<LabAttempt, Integer> {

    /**
     * Tìm lượt làm bài đầu tiên thỏa mãn bộ lọc Lab, User và Trạng thái.
     * Thường dùng để tìm lượt đang làm dở (status = 'in_progress').
     * * Tương thích DB: Spring Data JPA tự động dịch 'findFirst' sang 'SELECT TOP 1' (SQL Server)
     * hoặc 'LIMIT 1' (MySQL/TiDB) một cách chính xác.
     */
    Optional<LabAttempt> findFirstByLabIdAndUserIdAndStatus(Integer labId, Integer userId, String status);

    /**
     * Tìm một lượt làm bài dựa trên ID và ID người dùng.
     * Phương thức này cực kỳ quan trọng để đảm bảo tính bảo mật (Security check):
     * Học viên chỉ có quyền truy cập/xem kết quả bài làm của chính mình.
     *
     * @param attemptId ID của lượt làm bài
     * @param userId ID của người dùng sở hữu
     */
    Optional<LabAttempt> findByIdAndUserId(Integer attemptId, Integer userId);

    /**
     * Lấy lượt làm bài mới nhất (gần đây nhất) của một học viên cho một bài Lab cụ thể.
     * Sắp xếp theo thời gian bắt đầu (startedAt) giảm dần và lấy bản ghi đầu tiên.
     * * @param labId ID của bài Lab
     * @param userId ID của học viên
     */
    Optional<LabAttempt> findFirstByLabIdAndUserIdOrderByStartedAtDesc(Integer labId, Integer userId);
}