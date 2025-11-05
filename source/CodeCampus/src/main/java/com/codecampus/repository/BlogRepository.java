package com.codecampus.repository;

import com.codecampus.entity.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BlogRepository extends JpaRepository<Blog, Long> {
    // Tìm 4 bài viết đầu tiên theo status, sắp xếp theo ngày publish giảm dần
    List<Blog> findTop4ByStatusOrderByPublishedAtDesc(String status);

    // Tìm 5 bài viết đầu tiên theo status, sắp xếp theo ngày publish giảm dần
    List<Blog> findTop5ByStatusOrderByPublishedAtDesc(String status);

    /**
     * SỬA LẠI: Tìm tất cả bài blog đã xuất bản, sắp xếp theo NGÀY CẬP NHẬT
     */
    Page<Blog> findByStatusOrderByUpdatedAtDesc(String status, Pageable pageable);

    /**
     * MỚI: Tìm kiếm theo Tiêu đề (Title) VÀ Sắp xếp theo NGÀY CẬP NHẬT
     */
    Page<Blog> findByTitleContainingIgnoreCaseAndStatusOrderByUpdatedAtDesc(String keyword, String status, Pageable pageable);

    /**
     * MỚI: Lấy 5 bài viết mới nhất cho Sidebar
     */
    List<Blog> findTop5ByStatusOrderByUpdatedAtDesc(String status);

    /**
     * Giữ nguyên: Tìm chi tiết bài viết
     */
    Optional<Blog> findByIdAndStatus(Integer id, String status);
}
