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

    @Query("SELECT q FROM Question q " +
            "LEFT JOIN q.quizzes z " + // Giả sử quan hệ ManyToMany
            "WHERE (:keyword IS NULL OR q.content LIKE %:keyword%) " +
            "AND (:courseId IS NULL OR q.course.id = :courseId) " +
            "AND (:quizId IS NULL OR z.id = :quizId) " +
            "AND (:levelId IS NULL OR q.questionLevel.id = :levelId)")
    Page<Question> findQuestionsAdmin(String keyword, Integer courseId, Integer quizId, Integer levelId, Pageable pageable);
}
