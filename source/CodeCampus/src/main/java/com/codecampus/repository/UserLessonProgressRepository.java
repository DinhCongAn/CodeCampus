package com.codecampus.repository;

import com.codecampus.entity.UserLessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository quản lý tiến độ học tập chi tiết của từng bài học (User Lesson Progress).
 * Dùng để đánh dấu bài học đã hoàn thành và tính toán phần trăm tiến độ tổng thể.
 * Thiết kế đảm bảo tương thích 100% với SQL Server (Local) và TiDB/MySQL (Render).
 */
@Repository
public interface UserLessonProgressRepository extends JpaRepository<UserLessonProgress, Long> {

    /**
     * Tìm bản ghi tiến độ của một người dùng đối với một bài học cụ thể.
     * Tương thích DB: Hibernate tự động sinh câu lệnh SELECT dựa trên tên phương thức.
     * * @param userId ID người dùng (Integer)
     * @param lessonId ID bài học (Integer)
     * @return Đối tượng UserLessonProgress nếu người dùng đã từng truy cập bài học này
     */
    UserLessonProgress findByUserIdAndLessonId(Integer userId, Integer lessonId);

    /**
     * Đếm số lượng bài học đã hoàn thành (isCompleted = true) của một người dùng trong một khóa học.
     * Kết quả của hàm này là "tử số" để tính công thức: (Số bài đã học / Tổng số bài) * 100.
     * * Tương thích DB: Sử dụng JPQL chuẩn, Hibernate sẽ tự động xử lý kiểu dữ liệu Boolean
     * (bit trong SQL Server hoặc tinyint trong MySQL/TiDB) một cách chính xác.
     * * @param userId ID người dùng
     * @param courseId ID khóa học
     * @return Tổng số bài học đã nhấn nút "Hoàn thành"
     */
    @Query("SELECT COUNT(u) FROM UserLessonProgress u WHERE u.user.id = :userId AND u.course.id = :courseId AND u.isCompleted = true")
    long countCompletedLessons(@Param("userId") Integer userId, @Param("courseId") Integer courseId);
}