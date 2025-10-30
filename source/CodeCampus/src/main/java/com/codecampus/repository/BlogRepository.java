package com.codecampus.repository;

import com.codecampus.entity.Blog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlogRepository extends JpaRepository<Blog, Long> {
    // Tìm 4 bài viết đầu tiên theo status, sắp xếp theo ngày publish giảm dần
    List<Blog> findTop4ByStatusOrderByPublishedAtDesc(String status);

    // Tìm 5 bài viết đầu tiên theo status, sắp xếp theo ngày publish giảm dần
    List<Blog> findTop5ByStatusOrderByPublishedAtDesc(String status);
}
