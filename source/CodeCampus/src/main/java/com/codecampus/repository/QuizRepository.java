package com.codecampus.repository;

import com.codecampus.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Integer> {
    // Lấy Quiz (bao gồm câu hỏi) bằng ID
    @Query("SELECT q FROM Quiz q LEFT JOIN FETCH q.questions WHERE q.id = ?1")
    Optional<Quiz> findByIdWithQuestions(Integer quizId);
}