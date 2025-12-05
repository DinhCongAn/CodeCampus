package com.codecampus.repository;

import com.codecampus.entity.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BlogRepository extends JpaRepository<Blog, Long> {
    // Tìm 4 bài viết đầu tiên theo status, sắp xếp theo ngày publish giảm dần
    List<Blog> findTop4ByStatusAndIsFeaturedTrueOrderByPublishedAtDesc(String status);

    // Tìm 5 bài viết đầu tiên theo status, sắp xếp theo ngày publish giảm dần
    List<Blog> findTop5ByStatusOrderByCreatedAtDesc(String status);

    // 1. CHỈ TÌM KIẾM THEO TỪ KHÓA (Title)
    // Sắp xếp theo ngày cập nhật (thay vì publishedAt)
    Page<Blog> findByStatusAndTitleContainingIgnoreCaseOrderByUpdatedAtDesc(String status, String keyword, Pageable pageable);

    // 2. CHỈ LỌC THEO DANH MỤC (Category)
    Page<Blog> findByStatusAndCategoryIdOrderByUpdatedAtDesc(String status, Integer categoryId, Pageable pageable);

    // 3. KẾT HỢP: LỌC THEO DANH MỤC VÀ TÌM THEO TỪ KHÓA
    Page<Blog> findByStatusAndTitleContainingIgnoreCaseAndCategoryIdOrderByUpdatedAtDesc(String status, String keyword, Integer categoryId, Pageable pageable);

    // 4. MẶC ĐỊNH: Lấy tất cả (đã sửa để sắp xếp theo ngày cập nhật)
    Page<Blog> findByStatusOrderByUpdatedAtDesc(String status, Pageable pageable);
    List<Blog> findTop5ByStatusOrderByUpdatedAtDesc(String status);
    Optional<Blog> findByIdAndStatus(Integer id, String status);
    List<Blog> findByAuthorId(Integer authorId);
    // Tìm kiếm & Lọc (Cho Admin) - Hỗ trợ phân trang
    // COALESCE giúp xử lý trường hợp tham số truyền vào là null (nghĩa là không lọc theo trường đó)
    @Query("SELECT b FROM Blog b WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR b.title LIKE %:keyword%) AND " +
            "(:categoryId IS NULL OR b.category.id = :categoryId) AND " +
            "(:status IS NULL OR :status = '' OR b.status = :status)")
    Page<Blog> searchBlogs(@Param("keyword") String keyword,
                           @Param("categoryId") Integer categoryId,
                           @Param("status") String status,
                           Pageable pageable);

}
