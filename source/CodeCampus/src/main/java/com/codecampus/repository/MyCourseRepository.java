package com.codecampus.repository;

import com.codecampus.entity.MyCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MyCourseRepository extends JpaRepository<MyCourse, Integer> {

    boolean existsByUserIdAndCourseId(Integer userId, Integer courseId);
    // Tìm thông tin tiến độ tổng quan của User trong 1 Course
    MyCourse findByUserIdAndCourseId(Integer userId, Integer courseId);
    List<MyCourse> findByUserId(Integer userId);

    long countByCourse_Id(Long courseId);

    // 1. Đếm tổng số học viên (User) duy nhất
    @Query("SELECT COUNT(DISTINCT m.user.id) FROM MyCourse m")
    long countDistinctStudents();

    // 2. Đếm số lượng học viên hoạt động hôm nay (Dựa trên lastAccessed)
    // Lưu ý: Đây là số người "có vào học hôm nay", chính xác hơn là "đăng ký mới".
    // Nếu muốn chính xác "đăng ký mới", bạn nên query bảng Registration.
    @Query("SELECT COUNT(DISTINCT m.user.id) FROM MyCourse m WHERE m.lastAccessed >= :startOfDay")
    long countActiveStudentsToday(@Param("startOfDay") LocalDateTime startOfDay);

    // 3. Tìm số lượng enroll của khóa học đông nhất
    // Trả về List<Long>, phần tử đầu tiên là số lượng lớn nhất
    @Query("SELECT COUNT(m) as enrollmentCount FROM MyCourse m GROUP BY m.course.id ORDER BY enrollmentCount DESC")
    List<Long> findTopCourseEnrollmentCounts();

    // 4. Đếm số lượng khóa học đã hoàn thành (Progress >= 100)
    @Query("SELECT COUNT(m) FROM MyCourse m WHERE m.progressPercent >= 100")
    long countCompletedCourses();
}