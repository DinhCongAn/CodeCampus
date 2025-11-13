package com.codecampus.repository;

import com.codecampus.entity.QuizAttemptAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptAnswerRepository extends JpaRepository<QuizAttemptAnswer, Integer> {

    // Lấy danh sách câu trả lời của 1 lần làm quiz
    List<QuizAttemptAnswer> findByAttempt_Id(Integer attemptId);

    // Tìm câu trả lời cho 1 câu hỏi cụ thể trong 1 attempt
    Optional<QuizAttemptAnswer> findByAttempt_IdAndQuestion_Id(Integer attemptId, Integer questionId);
}
