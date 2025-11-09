package com.codecampus.repository;

import com.codecampus.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByIsFeaturedAndStatus(boolean isFeatured, String status);
    // Trạng thái mặc định
    String STATUS_PUBLISHED = "published";

    // --- CÁC PHƯƠNG THỨC CHO TRANG DANH SÁCH (Courses List) ---

    // 1. Mặc định: Lấy tất cả (phân trang, sắp xếp theo ngày cập nhật)
    Page<Course> findByStatusOrderByUpdatedAtDesc(String status, Pageable pageable);

    // 2. Tìm kiếm theo Tên (Name)
    Page<Course> findByStatusAndNameContainingIgnoreCaseOrderByUpdatedAtDesc(String status, String keyword, Pageable pageable);

    // 3. Lọc theo Danh mục (Category)
    Page<Course> findByStatusAndCategoryIdOrderByUpdatedAtDesc(String status, Integer categoryId, Pageable pageable);

    // 4. Kết hợp: Tìm kiếm Tên VÀ Lọc theo Danh mục
    Page<Course> findByStatusAndNameContainingIgnoreCaseAndCategoryIdOrderByUpdatedAtDesc(String status, String keyword, Integer categoryId, Pageable pageable);

    // --- CÁC PHƯƠNG THỨC CHO SIDEBAR ---

    // 5. Lấy các khóa học nổi bật (Featured Subjects) cho Sidebar
    List<Course> findTop5ByStatusAndIsFeaturedOrderByUpdatedAtDesc(String status, boolean isFeatured);

    // --- PHƯƠNG THỨC CHO TRANG CHI TIẾT (Course Details) ---

    // 6. Lấy chi tiết khóa học
    Optional<Course> findByIdAndStatus(Integer id, String status);
}
