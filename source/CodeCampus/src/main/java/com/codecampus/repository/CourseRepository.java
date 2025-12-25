package com.codecampus.repository;

import com.codecampus.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository quản lý các khóa học (Course).
 * Thiết kế đảm bảo hoạt động đồng nhất trên cả SQL Server và MySQL/TiDB.
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    /**
     * Tìm danh sách khóa học theo tính năng nổi bật và trạng thái.
     */
    List<Course> findByIsFeaturedAndStatus(boolean isFeatured, String status);

    /**
     * Tìm tất cả khóa học dựa trên trạng thái (ví dụ: 'published', 'draft').
     */
    List<Course> findCourseByStatus(String status);

    // --- CÁC PHƯƠNG THỨC CHO TRANG DANH SÁCH (Courses List) ---

    /**
     * Lấy tất cả khóa học theo trạng thái, sắp xếp theo ngày cập nhật mới nhất.
     * Tương thích DB: Hibernate tự dịch phân trang cho từng hệ quản trị (OFFSET/LIMIT hoặc TOP).
     */
    Page<Course> findByStatusOrderByUpdatedAtDesc(String status, Pageable pageable);

    /**
     * Tìm kiếm theo tên (Name) và trạng thái.
     * 'ContainingIgnoreCase' giúp tìm kiếm không phân biệt hoa thường và không cần manual COLLATE.
     */
    Page<Course> findByStatusAndNameContainingIgnoreCaseOrderByUpdatedAtDesc(String status, String keyword, Pageable pageable);

    /**
     * Lọc khóa học theo danh mục (Category).
     */
    Page<Course> findByStatusAndCategoryIdOrderByUpdatedAtDesc(String status, Integer categoryId, Pageable pageable);

    /**
     * Kết hợp: Tìm kiếm theo tên VÀ lọc theo danh mục cho người dùng.
     */
    Page<Course> findByStatusAndNameContainingIgnoreCaseAndCategoryIdOrderByUpdatedAtDesc(String status, String keyword, Integer categoryId, Pageable pageable);

    // --- CÁC PHƯƠNG THỨC CHO SIDEBAR ---

    /**
     * Lấy 5 khóa học nổi bật mới nhất cho Sidebar.
     * Tương thích DB: 'Top5' được Hibernate dịch thành 'TOP 5' (SQL Server) hoặc 'LIMIT 5' (MySQL).
     */
    List<Course> findTop5ByStatusAndIsFeaturedOrderByUpdatedAtDesc(String status, boolean isFeatured);

    // --- PHƯƠNG THỨC CHO TRANG CHI TIẾT ---

    /**
     * Lấy chi tiết khóa học. Giữ nguyên ID Integer theo thiết kế gốc của bạn.
     */
    Optional<Course> findByIdAndStatus(Integer id, String status);

    /**
     * Thống kê số lượng khóa học được tạo trong một khoảng thời gian cụ thể.
     */
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Truy vấn tìm kiếm và lọc cho giao diện Quản trị (Admin).
     * Sử dụng JPQL chuẩn (Java Persistence Query Language):
     * - Giúp xử lý tham số null một cách linh hoạt.
     * - CONCAT và LOWER giúp tìm kiếm đồng nhất trên mọi loại Database.
     */
    @Query("SELECT c FROM Course c " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:categoryId IS NULL OR c.category.id = :categoryId) " +
            "AND (:status IS NULL OR c.status = :status) " +
            "ORDER BY c.createdAt DESC")
    Page<Course> findCoursesAdmin(@Param("keyword") String keyword,
                                  @Param("categoryId") Integer categoryId,
                                  @Param("status") String status,
                                  Pageable pageable);

    /**
     * Tìm khóa học theo tên chính xác.
     */
    Optional<Course> findByName(String name);

    /**
     * Tìm kiếm nhanh theo tên và trạng thái có phân trang.
     */
    Page<Course> findByNameContainingIgnoreCaseAndStatus(String name, String status, Pageable pageable);
}