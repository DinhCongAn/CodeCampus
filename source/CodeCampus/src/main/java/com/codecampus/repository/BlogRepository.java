package com.codecampus.repository;

import com.codecampus.entity.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository quản lý các bài viết tin tức (Blog).
 * Hỗ trợ các tính năng tìm kiếm, phân trang và lọc theo danh mục.
 */
@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {

    /**
     * Tìm danh sách bài viết nổi bật (Featured) mới nhất.
     * Spring Data JPA sẽ tự động dịch "Top4" thành "LIMIT 4" trong MySQL/TiDB.
     */
    List<Blog> findTop4ByStatusAndIsFeaturedTrueOrderByPublishedAtDesc(String status);

    /**
     * Lấy 5 bài viết mới nhất dựa trên thời gian tạo.
     */
    List<Blog> findTop5ByStatusOrderByCreatedAtDesc(String status);

    /**
     * Tìm kiếm bài viết theo tiêu đề (Title) có phân trang.
     * IgnoreCase đảm bảo tìm kiếm không phân biệt hoa thường.
     */
    Page<Blog> findByStatusAndTitleContainingIgnoreCaseOrderByUpdatedAtDesc(String status, String keyword, Pageable pageable);

    /**
     * Lọc bài viết theo ID danh mục.
     */
    Page<Blog> findByStatusAndCategoryIdOrderByUpdatedAtDesc(String status, Integer categoryId, Pageable pageable);

    /**
     * Kết hợp lọc theo danh mục và tìm kiếm theo tiêu đề.
     */
    Page<Blog> findByStatusAndTitleContainingIgnoreCaseAndCategoryIdOrderByUpdatedAtDesc(String status, String keyword, Integer categoryId, Pageable pageable);

    /**
     * Truy vấn mặc định lấy toàn bộ bài viết theo trạng thái, sắp xếp theo ngày cập nhật.
     */
    Page<Blog> findByStatusOrderByUpdatedAtDesc(String status, Pageable pageable);

    /**
     * Lấy top 5 bài viết mới cập nhật nhất cho Sidebar hoặc Footer.
     */
    List<Blog> findTop5ByStatusOrderByUpdatedAtDesc(String status);

    /**
     * Tìm chi tiết bài viết theo ID và trạng thái.
     * Lưu ý: Sử dụng Integer cho ID để khớp với kiểu dữ liệu INT trong cơ sở dữ liệu.
     */
    Optional<Blog> findByIdAndStatus(Integer id, String status);

    /**
     * Tìm tất cả bài viết của một tác giả cụ thể.
     */
    List<Blog> findByAuthorId(Integer authorId);

    /**
     * Tìm kiếm bài viết linh hoạt cho giao diện quản trị (Admin).
     * Sử dụng JPQL chuẩn:
     * - Tự động tương thích với TiDB/MySQL.
     * - Xử lý trường hợp các tham số lọc là null hoặc rỗng.
     */
    @Query("SELECT b FROM Blog b WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR b.title LIKE %:keyword%) AND " +
            "(:categoryId IS NULL OR b.category.id = :categoryId) AND " +
            "(:status IS NULL OR :status = '' OR b.status = :status)")
    Page<Blog> searchBlogs(@Param("keyword") String keyword,
                           @Param("categoryId") Integer categoryId,
                           @Param("status") String status,
                           Pageable pageable);

    /**
     * Tìm kiếm bài viết theo tiêu đề và trạng thái cụ thể.
     */
    Page<Blog> findByTitleContainingIgnoreCaseAndStatus(String title, String status, Pageable pageable);

    /**
     * Lấy danh sách các bài viết liên quan (cùng danh mục) trừ bài viết hiện tại.
     * Thường dùng cho phần "Bài viết liên quan" trong trang chi tiết.
     */
    List<Blog> findTop2ByCategoryIdAndIdNotAndStatusOrderByCreatedAtDesc(
            Integer categoryId, Integer currentId, String status
    );
}