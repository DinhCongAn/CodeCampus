package com.codecampus.repository;

import com.codecampus.entity.MyCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository quản lý tiến độ học tập và danh sách khóa học của người dùng (MyCourse).
 * Cung cấp các thống kê về học viên và mức độ hoàn thành khóa học.
 */
@Repository
public interface MyCourseRepository extends JpaRepository<MyCourse, Integer> {

    /**
     * Kiểm tra xem người dùng đã sở hữu khóa học này chưa.
     */
    boolean existsByUserIdAndCourseId(Integer userId, Integer courseId);

    /**
     * Tìm thông tin tiến độ tổng quan của một người dùng trong một khóa học cụ thể.
     */
    MyCourse findByUserIdAndCourseId(Integer userId, Integer courseId);

    /**
     * Lấy danh sách tất cả các khóa học mà người dùng đang tham gia.
     */
    List<MyCourse> findByUserId(Integer userId);

    /**
     * Đếm tổng số lượt đăng ký của một khóa học cụ thể.
     * Sử dụng tham số kiểu Long để khớp với thiết kế Entity Course của bạn.
     */
    long countByCourse_Id(Long courseId);

    /**
     * Thống kê tổng số lượng học viên duy nhất (Unique Students) trong hệ thống.
     * Sử dụng JPQL chuẩn: Đảm bảo hoạt động đồng nhất trên SQL Server và MySQL.
     */
    @Query("SELECT COUNT(DISTINCT m.user.id) FROM MyCourse m")
    long countDistinctStudents();

    /**
     * Đếm số lượng học viên có hoạt động học tập (truy cập bài học) kể từ một thời điểm cụ thể.
     * @param startOfDay Thời điểm bắt đầu tính (thường là bắt đầu ngày hôm nay).
     */
    @Query("SELECT COUNT(DISTINCT m.user.id) FROM MyCourse m WHERE m.lastAccessed >= :startOfDay")
    long countActiveStudentsToday(@Param("startOfDay") LocalDateTime startOfDay);

    /**
     * Lấy danh sách số lượng học viên của các khóa học, sắp xếp từ đông nhất đến ít nhất.
     * Tương thích DB: Hibernate xử lý GROUP BY và ORDER BY đồng nhất cho mọi hệ quản trị.
     * Phần tử đầu tiên trong danh sách kết quả sẽ là số lượng enroll lớn nhất.
     */
    @Query("SELECT COUNT(m) as enrollmentCount FROM MyCourse m GROUP BY m.course.id ORDER BY enrollmentCount DESC")
    List<Long> findTopCourseEnrollmentCounts();

    /**
     * Thống kê tổng số lượt hoàn thành khóa học trên toàn hệ thống (Tiến độ >= 100%).
     */
    @Query("SELECT COUNT(m) FROM MyCourse m WHERE m.progressPercent >= 100")
    long countCompletedCourses();
}