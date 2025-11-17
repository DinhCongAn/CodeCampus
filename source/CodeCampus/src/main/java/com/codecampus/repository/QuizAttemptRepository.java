package com.codecampus.repository;

import com.codecampus.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Integer> {

    /**
     * Tìm tất cả các lần làm bài của một user cho một quiz cụ thể
     * Sắp xếp theo thời gian bắt đầu, mới nhất lên trước.
     */
    List<QuizAttempt> findByUserIdAndQuizIdOrderByStartTimeDesc(Integer userId, Integer quizId);

    @Query("SELECT qa FROM QuizAttempt qa " +
            "JOIN FETCH qa.user " +
            "JOIN FETCH qa.quiz " +
            "WHERE qa.id = :attemptId AND qa.status = 'completed'")
    Optional<QuizAttempt> findCompletedAttemptById(@Param("attemptId") Integer attemptId);


}