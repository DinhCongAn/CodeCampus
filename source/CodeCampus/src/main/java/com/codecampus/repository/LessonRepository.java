package com.codecampus.repository;

import com.codecampus.entity.Lesson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repository quản lý các bài học (Lesson) trong khóa học.
 * Hỗ trợ các logic về thứ tự bài học (Order Number) và liên kết với Lab/Quiz.
 */
@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    /**
     * Lấy danh sách bài học thuộc một khóa học, sắp xếp theo thứ tự hiển thị.
     */
    List<Lesson> findByCourseIdAndStatusOrderByOrderNumberAsc(Integer courseId, String status);

    /**
     * Tìm bài học đầu tiên (mở đầu) của một khóa học.
     * Tương thích: Hibernate tự dịch sang TOP 1 (SQL Server) hoặc LIMIT 1 (MySQL/TiDB).
     */
    Optional<Lesson> findFirstByCourseIdAndStatusOrderByOrderNumberAsc(Integer courseId, String status);

    /**
     * Tìm bài học liên kết với một bài thực hành (Lab) cụ thể.
     */
    Optional<Lesson> findByLabId(Integer labId);

    /**
     * Tìm bài học liên kết với một bài kiểm tra (Quiz) cụ thể.
     */
    Optional<Lesson> findByQuizId(Integer quizId);

    /**
     * Tìm tất cả bài học có sử dụng chung một bài kiểm tra.
     */
    List<Lesson> findAllByQuizId(Integer quizId);

    /**
     * Đếm tổng số bài học của một khóa học theo trạng thái.
     */
    long countByCourseIdAndStatus(Integer courseId, String status);

    /**
     * Tìm tất cả bài học của khóa học (không phân biệt trạng thái).
     */
    List<Lesson> findByCourseId(Integer courseId);

    /**
     * [ADMIN] Tìm kiếm bài học nâng cao có lọc theo từ khóa, loại bài học và trạng thái.
     * Tương thích DB: Sử dụng LOWER và CONCAT chuẩn JPQL chạy được trên mọi hệ quản trị.
     */
    @Query("SELECT l FROM Lesson l WHERE l.course.id = :courseId " +
            "AND (:keyword IS NULL OR :keyword = '' OR LOWER(l.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:typeId IS NULL OR l.lessonType.id = :typeId) " +
            "AND (:status IS NULL OR :status = '' OR l.status = :status) " +
            "ORDER BY l.orderNumber ASC")
    Page<Lesson> findLessonsByCourse(@Param("courseId") Long courseId,
                                     @Param("keyword") String keyword,
                                     @Param("typeId") Integer typeId,
                                     @Param("status") String status,
                                     Pageable pageable);

    /**
     * Tìm giá trị số thứ tự lớn nhất hiện tại của khóa học.
     * COALESCE giúp trả về 0 nếu khóa học chưa có bài học nào, tránh lỗi NullPointerException.
     */
    @Query("SELECT COALESCE(MAX(l.orderNumber), 0) FROM Lesson l WHERE l.course.id = :courseId")
    Integer findMaxOrderNumber(@Param("courseId") Long courseId);

    /**
     * Kiểm tra xem một số thứ tự (Order Number) đã tồn tại trong khóa học chưa.
     */
    @Query("SELECT COUNT(l) > 0 FROM Lesson l " +
            "WHERE l.course.id = :courseId " +
            "AND l.orderNumber = :orderNumber " +
            "AND (:lessonId IS NULL OR l.id != :lessonId)")
    boolean existsByOrderNumber(@Param("courseId") Long courseId,
                                @Param("orderNumber") Integer orderNumber,
                                @Param("lessonId") Long lessonId);

    /**
     * Tự động đẩy lùi số thứ tự của các bài học đứng sau bài học mới chèn (Shift Down).
     * @Modifying: Đánh dấu lệnh cập nhật dữ liệu.
     * @Transactional: Đảm bảo toàn bộ quá trình dịch chuyển thứ tự được thực hiện an toàn.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Lesson l SET l.orderNumber = l.orderNumber + 1 " +
            "WHERE l.course.id = :courseId AND l.orderNumber >= :orderNumber")
    void shiftOrdersDown(@Param("courseId") Long courseId,
                         @Param("orderNumber") Integer orderNumber);

    /**
     * Kiểm tra trùng tên bài học trong phạm vi một khóa học (không phân biệt hoa thường).
     */
    @Query("SELECT COUNT(l) > 0 FROM Lesson l " +
            "WHERE l.course.id = :courseId " +
            "AND LOWER(l.name) = LOWER(:name) " +
            "AND (:lessonId IS NULL OR l.id != :lessonId)")
    boolean existsByName(@Param("courseId") Long courseId,
                         @Param("name") String name,
                         @Param("lessonId") Long lessonId);
}