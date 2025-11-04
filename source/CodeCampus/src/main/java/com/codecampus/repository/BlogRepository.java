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
     * Tìm tất cả bài blog theo status (ví dụ: 'published') và sắp xếp theo ngày
     * @param status Trạng thái (vd: "published")
     * @param pageable Thông tin phân trang (Pageable)
     * @return Một trang (Page) chứa các bài Blog
     */
    Page<Blog> findByStatusOrderByPublishedAtDesc(String status, Pageable pageable);

    /**
     * Tìm một bài blog cụ thể bằng ID và phải có status là 'published'
     * @param id ID của bài blog
     * @param status Trạng thái (vd: "published")
     * @return Optional chứa Blog nếu tìm thấy
     */
    Optional<Blog> findByIdAndStatus(Integer id, String status);
}
