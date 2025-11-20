package com.codecampus.repository;

import com.codecampus.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    /**
     * Tìm tất cả bài học thuộc một khóa học (courseId),
     * và sắp xếp chúng theo order_number (thứ tự bài học)
     */
    List<Lesson> findByCourseIdOrderByOrderNumberAsc(Integer courseId);

    // ===== BỔ SUNG PHƯƠNG THỨC NÀY =====
    /**
     * Tìm bài học ĐẦU TIÊN (có orderNumber nhỏ nhất) của một khóa học.
     */
    Optional<Lesson> findFirstByCourseIdOrderByOrderNumberAsc(Integer courseId);
    Optional<Lesson> findByLabId(Integer labId);
    Optional<Lesson> findByQuizId(Integer quizId);
}
