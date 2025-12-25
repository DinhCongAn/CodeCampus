package com.codecampus.repository;

import com.codecampus.entity.CourseCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository quản lý danh mục khóa học (Course Category).
 * Đã lược bỏ Native Query để đảm bảo chạy mượt trên cả SQL Server và TiDB/MySQL.
 */
@Repository
public interface CourseCategoryRepository extends JpaRepository<CourseCategory, Integer> {

    /**
     * Lấy danh sách các danh mục đang hoạt động.
     * Thường dùng cho Sidebar hoặc các dropdown lựa chọn.
     * * @param isActive Trạng thái hoạt động
     * @return Danh sách danh mục
     */
    List<CourseCategory> findByIsActive(boolean isActive);

    /**
     * Tìm kiếm danh mục theo tên chính xác (không phân biệt hoa thường).
     * Dùng để kiểm tra trùng lặp khi thêm mới hoặc cập nhật.
     * * @param name Tên danh mục
     * @return Đối tượng danh mục nếu tìm thấy
     */
    @Query("SELECT c FROM CourseCategory c WHERE LOWER(c.name) = LOWER(:name)")
    CourseCategory findByNameIgnoreCase(@Param("name") String name);

    /**
     * Truy vấn danh sách danh mục phục vụ giao diện Quản trị (Admin).
     * Đã chuyển từ Native Query sang JPQL chuẩn:
     * 1. Loại bỏ 'COLLATE Latin1_General_CI_AI' (chỉ có ở SQL Server) để tương thích TiDB.
     * 2. Sử dụng LOWER() và CONCAT() để tìm kiếm không phân biệt hoa thường một cách đồng nhất.
     * 3. Tự động hỗ trợ phân trang (Pageable) cho mọi loại Database.
     * * @param keyword Từ khóa tìm kiếm theo tên
     * @param isActive Bộ lọc theo trạng thái (true/false/null)
     * @param pageable Đối tượng phân trang
     * @return Trang kết quả danh mục
     */
    @Query("SELECT c FROM CourseCategory c " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:isActive IS NULL OR c.isActive = :isActive) " +
            "ORDER BY c.name ASC")
    Page<CourseCategory> findCategoriesAdmin(@Param("keyword") String keyword,
                                             @Param("isActive") Boolean isActive,
                                             Pageable pageable);
}