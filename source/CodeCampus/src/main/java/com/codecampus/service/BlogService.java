package com.codecampus.service;

import com.codecampus.entity.Blog;
import com.codecampus.repository.BlogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class BlogService {

    private final BlogRepository blogRepository;

    public BlogService(BlogRepository blogRepository) {
        this.blogRepository = blogRepository;
    }

    // Trạng thái mặc định
    private static final String STATUS_PUBLISHED = "published";

    /**
     * Lấy danh sách các bài blog đã xuất bản, có phân trang
     */
    public Page<Blog> getPublishedBlogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return blogRepository.findByStatusOrderByPublishedAtDesc(STATUS_PUBLISHED, pageable);
    }

    /**
     * Lấy chi tiết một bài blog đã xuất bản
     */
    public Blog getPublishedBlogById(Integer id) {
        return blogRepository.findByIdAndStatus(id, STATUS_PUBLISHED)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài blog hoặc chưa được xuất bản."));
    }
}