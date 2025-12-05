package com.codecampus.repository;

import com.codecampus.entity.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Integer> {

    // 1. Tìm kiếm nâng cao (Tên + Môn học + Loại)
    @Query("SELECT q FROM Quiz q " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR LOWER(q.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:courseId IS NULL OR q.course.id = :courseId) " +
            "AND (:typeId IS NULL OR q.testType.id = :typeId) " +
            "ORDER BY q.id DESC")
    Page<Quiz> findQuizzesAdmin(@Param("keyword") String keyword,
                                @Param("courseId") Integer courseId, // Sửa Integer/Long tùy Entity Course của bạn
                                @Param("typeId") Integer typeId,
                                Pageable pageable);

    // 2. Đếm số câu hỏi trong 1 Quiz
    @Query("SELECT COUNT(q) FROM Quiz quiz JOIN quiz.questions q WHERE quiz.id = :quizId")
    Integer countQuestionsByQuizId(@Param("quizId") Integer quizId);

    // 3. Kiểm tra xem Quiz đã có người làm chưa (Dựa vào bảng quiz_attempts)
    @Query("SELECT COUNT(qa) > 0 FROM QuizAttempt qa WHERE qa.quiz.id = :quizId")
    boolean hasAttempts(@Param("quizId") Integer quizId);
    // [MỚI] Check trùng tên Quiz trong cùng 1 Course
    @Query("SELECT COUNT(q) > 0 FROM Quiz q " +
            "WHERE q.course.id = :courseId " +
            "AND LOWER(q.name) = LOWER(:name) " +
            "AND (:quizId IS NULL OR q.id != :quizId)")
    boolean existsByNameAndCourse(@Param("name") String name,
                                  @Param("courseId") Integer courseId, // Hoặc Long tùy Entity Course
                                  @Param("quizId") Integer quizId);

    List<Quiz> findByCourseId(Long courseId); // Hoặc Integer tùy Entity Course

}