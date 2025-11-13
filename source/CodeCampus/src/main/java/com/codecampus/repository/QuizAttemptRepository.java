package com.codecampus.repository;

import com.codecampus.entity.LabAttempt;
import com.codecampus.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Integer> {

    Optional<QuizAttempt> findFirstByUserIdAndQuizIdOrderByStartTimeDesc(Integer userId, Integer quizId);

    // Truy vấn lấy attempt đang làm + load chi tiết quiz/lesson/course/questions
    @Query("""
        SELECT qa FROM QuizAttempt qa
        JOIN FETCH qa.quiz q
        JOIN FETCH q.lesson l
        JOIN FETCH l.course c
        LEFT JOIN FETCH q.questions
        WHERE qa.id = :attemptId
          AND qa.user.id = :userId
          AND qa.status = 'in_progress'
    """)
    Optional<QuizAttempt> findInProgressAttemptByIdAndUserWithDetails(
            @Param("attemptId") Integer attemptId,
            @Param("userId") Integer userId
    );

    // Truy vấn cho Lab (nếu có)
    Optional<LabAttempt> findByIdAndUserId(Integer attemptId, Integer userId);

    @Query("""
        SELECT qa FROM QuizAttempt qa
        JOIN FETCH qa.quiz q
        JOIN FETCH q.lesson l
        JOIN FETCH l.course c
        WHERE qa.id = :attemptId
          AND qa.user.id = :userId
    """)
    Optional<QuizAttempt> findByIdAndUserIdWithDetails(
            @Param("attemptId") Integer attemptId,
            @Param("userId") Integer userId
    );
}
