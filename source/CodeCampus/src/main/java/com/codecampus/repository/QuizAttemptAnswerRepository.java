package com.codecampus.repository;

import com.codecampus.entity.QuizAttemptAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository quản lý các câu trả lời chi tiết trong mỗi lượt làm bài kiểm tra (Quiz Attempt Answer).
 * Lưu trữ mối liên kết giữa lượt làm bài, câu hỏi và phương án học viên đã chọn.
 * Thiết kế đảm bảo hoạt động mượt mà trên cả SQL Server (Local) và TiDB/MySQL (Render).
 */
@Repository
public interface QuizAttemptAnswerRepository extends JpaRepository<QuizAttemptAnswer, Integer> {

    /**
     * Lấy danh sách toàn bộ các câu trả lời thuộc về một lượt làm bài cụ thể.
     * Tương thích DB: Hibernate tự động ánh xạ quan hệ Join giữa bảng QuizAttempt và QuizAttemptAnswer.
     * * @param attemptId ID của lượt làm bài kiểm tra
     * @return Danh sách các câu trả lời chi tiết
     */
    List<QuizAttemptAnswer> findByAttempt_Id(Integer attemptId);

    /**
     * Tìm kiếm câu trả lời của một câu hỏi cụ thể trong một lượt làm bài nhất định.
     * Thường dùng để kiểm tra xem học viên đã trả lời câu hỏi đó chưa hoặc lấy lại phương án đã chọn.
     * * @param attemptId ID của lượt làm bài
     * @param questionId ID của câu hỏi cần tìm câu trả lời
     * @return Đối tượng câu trả lời bọc trong Optional để xử lý an toàn tránh lỗi Null
     */
    Optional<QuizAttemptAnswer> findByAttempt_IdAndQuestion_Id(Integer attemptId, Integer questionId);
}