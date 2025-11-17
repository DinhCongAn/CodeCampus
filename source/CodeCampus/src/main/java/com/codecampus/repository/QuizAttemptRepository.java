package com.codecampus.repository;

import com.codecampus.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List; // (Bạn có thể cần import này)

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Integer> {

    // (Bạn có thể có các phương thức khác ở đây...)

    // === PHƯƠNG THỨC BỊ LỖI LÀ ĐÂY ===
    /**
     * Sửa lại truy vấn này.
     * BỎ: JOIN FETCH q.lesson l
     * BỎ: JOIN FETCH l.course c
     * THAY BẰNG: JOIN FETCH q.course c (Vì Quiz đã liên kết trực tiếp với Course)
     */
    @Query("SELECT qa FROM QuizAttempt qa " +
            "JOIN FETCH qa.quiz q " +
            "JOIN FETCH q.course c " + // <-- SỬA LẠI THÀNH DÒNG NÀY
            "WHERE qa.id = :attemptId AND qa.user.id = :userId")
    Optional<QuizAttempt> findByIdAndUserIdWithDetails(@Param("attemptId") Integer attemptId, @Param("userId") Integer userId);


    // (Bạn cũng cần sửa các phương thức khác nếu chúng dùng 'q.lesson')
}