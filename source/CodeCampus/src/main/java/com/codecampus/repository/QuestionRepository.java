package com.codecampus.repository;

import com.codecampus.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository <Question, Integer>{

    // ... (bên trong interface QuestionRepository)

    /**
     * Lấy tất cả câu hỏi của 1 Quiz, VÀ tải luôn các AnswerOptions
     */
    @Query("SELECT q FROM Question q " +
            "LEFT JOIN FETCH q.answerOptions " +
            "WHERE q.id IN (SELECT qq.id FROM Quiz quiz JOIN quiz.questions qq WHERE quiz.id = :quizId)")
    List<Question> findQuestionsByQuizIdWithOptions(@Param("quizId") Integer quizId);

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
    List<Question> findActiveQuestionsByQuizIdWithOptions(
            @Param("quizId") Integer quizId
    );


    // Bỏ "DISTINCT" và thay đổi cách lọc Quiz
    @Query("SELECT q FROM Question q " +
            "LEFT JOIN FETCH q.course c " +
            "LEFT JOIN FETCH q.lesson l " +
            "LEFT JOIN FETCH q.questionLevel ql " +
            "WHERE (:searchTest IS NULL OR q.content LIKE %:searchTest%) " +
            "AND (:subjectId IS NULL OR c.id = :subjectId) " +

            // SỬA ĐOẠN NÀY: Dùng Subquery thay vì Join trực tiếp để tránh duplicate
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
