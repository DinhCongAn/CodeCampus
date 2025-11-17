package com.codecampus.repository;

import com.codecampus.entity.AnswerOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface AnswerOptionRepository extends JpaRepository<AnswerOption, Integer> {

    // Lấy ID của tất cả các câu trả lời ĐÚNG cho một Quiz
    @Query("SELECT ao.id FROM AnswerOption ao " +
            "JOIN ao.question q " +
            "JOIN q.quizzes quiz " +
            "WHERE quiz.id = :quizId AND ao.isCorrect = true")
    Set<Integer> findCorrectAnswerIdsByQuizId(@Param("quizId") Integer quizId);
}