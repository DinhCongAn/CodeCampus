package com.codecampus.repository;

import com.codecampus.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository quản lý ngân hàng câu hỏi (Questions).
 * Hỗ trợ truy vấn phức tạp kết hợp bộ lọc và tối ưu hiệu năng bằng JOIN FETCH.
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {

    /**
     * Lấy danh sách câu hỏi của một bài kiểm tra (Quiz) kèm theo các phương án trả lời.
     * Sử dụng JOIN FETCH để tải dữ liệu AnswerOptions trong một câu truy vấn (Tránh lỗi N+1).
     *
     * @param quizId ID của bài kiểm tra
     * @return Danh sách câu hỏi kèm theo các lựa chọn trả lời
     */
    @Query("SELECT q FROM Question q " +
            "LEFT JOIN FETCH q.answerOptions " +
            "WHERE q.id IN (SELECT qq.id FROM Quiz quiz JOIN quiz.questions qq WHERE quiz.id = :quizId)")
    List<Question> findQuestionsByQuizIdWithOptions(@Param("quizId") Integer quizId);

    /**
     * Lấy danh sách các câu hỏi đang hoạt động (active) của một Quiz kèm theo phương án trả lời.
     * Sử dụng DISTINCT để đảm bảo không bị lặp lại bản ghi khi FETCH Collection.
     */
    @Query("""
        SELECT DISTINCT q
        FROM Question q
        LEFT JOIN FETCH q.answerOptions ao
        WHERE q.status = 'active'
          AND q.id IN (
              SELECT qq.id
              FROM Quiz quiz
              JOIN quiz.questions qq
              WHERE quiz.id = :quizId
          )
    """)
    List<Question> findActiveQuestionsByQuizIdWithOptions(@Param("quizId") Integer quizId);

    /**
     * Đếm tổng số lượng câu hỏi đang ở trạng thái hoạt động trong một Quiz.
     * Sử dụng EXISTS để tối ưu hiệu năng kiểm tra quan hệ Many-to-Many.
     */
    @Query("""
        SELECT COUNT(q)
        FROM Question q
        WHERE q.status = 'active'
          AND EXISTS (
              SELECT 1
              FROM q.quizzes quiz
              WHERE quiz.id = :quizId
          )
    """)
    int countActiveQuestionsByQuizId(@Param("quizId") Integer quizId);

    /**
     * Hệ thống lọc câu hỏi nâng cao dành cho giao diện Quản trị (Admin).
     * Tương thích đồng thời SQL Server & TiDB:
     * - Sử dụng LIKE với tham số để tìm kiếm chuỗi.
     * - Sử dụng EXISTS thay cho JOIN trực tiếp với Quizzes để tránh hiện tượng nhân đôi bản ghi (Duplicate) khi phân trang.
     * - Toàn bộ các thực thể liên quan (Course, Lesson, Level) được FETCH cùng lúc để tối ưu hiệu năng.
     *
     * @param search Từ khóa nội dung câu hỏi
     * @param subjectId Lọc theo khóa học
     * @param quizId Lọc theo bài kiểm tra
     * @param levelId Lọc theo độ khó
     * @param status Lọc theo trạng thái
     * @param pageable Tham số phân trang
     */
    @Query("SELECT q FROM Question q " +
            "LEFT JOIN FETCH q.course c " +
            "LEFT JOIN FETCH q.lesson l " +
            "LEFT JOIN FETCH q.questionLevel ql " +
            "WHERE (:searchTest IS NULL OR q.content LIKE %:searchTest%) " +
            "AND (:subjectId IS NULL OR c.id = :subjectId) " +
            "AND (:quizId IS NULL OR EXISTS (SELECT 1 FROM q.quizzes quiz WHERE quiz.id = :quizId)) " +
            "AND (:levelId IS NULL OR ql.id = :levelId) " +
            "AND (:status IS NULL OR q.status = :status) " +
            "AND q.status <> 'deleted'")
    Page<Question> findByFilters(
            @Param("searchTest") String search,
            @Param("subjectId") Integer subjectId,
            @Param("quizId") Integer quizId,
            @Param("levelId") Integer levelId,
            @Param("status") String status,
            Pageable pageable);
}