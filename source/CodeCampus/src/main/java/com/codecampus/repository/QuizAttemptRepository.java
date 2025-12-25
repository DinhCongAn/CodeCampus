package com.codecampus.repository;

import com.codecampus.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository quản lý các lượt làm bài kiểm tra (Quiz Attempt).
 * Lưu trữ thông tin về thời gian bắt đầu, kết thúc, điểm số và trạng thái bài làm.
 * Đảm bảo hoạt động ổn định trên cả SQL Server (Local) và TiDB/MySQL (Render).
 */
@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Integer> {

    /**
     * Tìm tất cả các lần làm bài của một học viên cho một bài Quiz cụ thể.
     * Kết quả được sắp xếp theo thời gian bắt đầu mới nhất lên đầu (Descending).
     * * Tương thích DB: Spring Data JPA tự động dịch OrderBy...Desc sang cú pháp chuẩn
     * của mọi hệ quản trị cơ sở dữ liệu.
     * * @param userId ID người dùng (Integer)
     * @param quizId ID bài kiểm tra (Integer)
     * @return Danh sách các lượt làm bài
     */
    List<QuizAttempt> findByUserIdAndQuizIdOrderByStartTimeDesc(Integer userId, Integer quizId);

    /**
     * Tìm chi tiết một lượt làm bài đã hoàn thành bằng ID.
     * * Tối ưu hiệu năng (Performance Optimization):
     * - Sử dụng "JOIN FETCH" để lấy đồng thời dữ liệu của User và Quiz trong 1 câu Query.
     * - Giúp tầng Service có thể gọi qa.getUser().getFullName() hoặc qa.getQuiz().getName()
     * mà không tốn thêm câu lệnh SQL phụ nào (Tránh lỗi N+1).
     * * Tương thích DB: JPQL đảm bảo chạy tốt trên cả SQL Server và MySQL/TiDB.
     * * @param attemptId ID của lượt làm bài
     * @return Optional chứa thông tin lượt làm bài nếu tìm thấy và đã hoàn thành
     */
    @Query("SELECT qa FROM QuizAttempt qa " +
            "JOIN FETCH qa.user " +
            "JOIN FETCH qa.quiz " +
            "WHERE qa.id = :attemptId AND qa.status = 'completed'")
    Optional<QuizAttempt> findCompletedAttemptById(@Param("attemptId") Integer attemptId);

}