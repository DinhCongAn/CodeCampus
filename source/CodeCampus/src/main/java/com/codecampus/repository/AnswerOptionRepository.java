package com.codecampus.repository;

import com.codecampus.entity.AnswerOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Repository quản lý các lựa chọn trả lời cho câu hỏi.
 */
@Repository
public interface AnswerOptionRepository extends JpaRepository<AnswerOption, Integer> {

    /**
     * Lấy danh sách ID của các câu trả lời đúng thuộc về một bài kiểm tra cụ thể.
     * Sử dụng JPQL để đảm bảo tính độc lập với hệ quản trị cơ sở dữ liệu (Database Independence).
     *
     * @param quizId ID của bài kiểm tra
     * @return Tập hợp các ID câu trả lời đúng
     */
    @Query("SELECT ao.id FROM AnswerOption ao " +
            "JOIN ao.question q " +
            "JOIN q.quizzes quiz " +
            "WHERE quiz.id = :quizId AND ao.isCorrect = true")
    Set<Integer> findCorrectAnswerIdsByQuizId(@Param("quizId") Integer quizId);

    /**
     * Xóa tất cả các lựa chọn trả lời liên quan đến một câu hỏi.
     * @Modifying: Đánh dấu đây là truy vấn thay đổi dữ liệu (DML).
     * @Transactional: Đảm bảo tính toàn vẹn dữ liệu, bắt buộc phải có cho lệnh DELETE/UPDATE.
     *
     * @param questionId ID của câu hỏi cần xóa câu trả lời
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM AnswerOption a WHERE a.question.id = :questionId")
    void deleteByQuestionId(@Param("questionId") Integer questionId);
}