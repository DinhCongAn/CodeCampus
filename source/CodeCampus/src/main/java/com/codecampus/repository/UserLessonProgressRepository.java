package com.codecampus.repository;

import com.codecampus.entity.UserLessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserLessonProgressRepository extends JpaRepository<UserLessonProgress, Long> {

    // Tìm record tiến độ của user với 1 bài học cụ thể
    UserLessonProgress findByUserIdAndLessonId(Integer userId, Integer lessonId);

    // Đếm số lượng bài học đã hoàn thành (isCompleted = true) trong 1 khóa học cụ thể
    // Dùng để tính tử số trong công thức: (Số bài đã học / Tổng số bài) * 100
    @Query("SELECT COUNT(u) FROM UserLessonProgress u WHERE u.user.id = :userId AND u.course.id = :courseId AND u.isCompleted = true")
    long countCompletedLessons(@Param("userId") Integer userId, @Param("courseId") Integer courseId);
}