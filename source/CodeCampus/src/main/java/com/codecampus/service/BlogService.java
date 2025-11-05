package com.codecampus.service;

import com.codecampus.entity.Blog;
import com.codecampus.entity.BlogCategory;
import com.codecampus.repository.BlogCategoryRepository;
import com.codecampus.repository.BlogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlogService {

    private final BlogRepository blogRepository;
    private final BlogCategoryRepository blogCategoryRepository; // Thêm repo này

    public BlogService(BlogRepository blogRepository, BlogCategoryRepository blogCategoryRepository) {
        this.blogRepository = blogRepository;
        this.blogCategoryRepository = blogCategoryRepository;
    }

    private static final String STATUS_PUBLISHED = "published";

    /**
     * CẬP NHẬT: Lấy danh sách blog (hỗ trợ cả tìm kiếm và phân trang)
     */
    public Page<Blog> getPublishedBlogs(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size);

        if (keyword != null && !keyword.isEmpty()) {
            // Nếu có từ khóa, thực hiện tìm kiếm
            return blogRepository.findByTitleContainingIgnoreCaseAndStatusOrderByUpdatedAtDesc(keyword, STATUS_PUBLISHED, pageable);
        } else {
            // Nếu không, lấy danh sách tiêu chuẩn
            return blogRepository.findByStatusOrderByUpdatedAtDesc(STATUS_PUBLISHED, pageable);
        }
    }

    /**
     * Giữ nguyên: Lấy chi tiết bài blog
     */
    public Blog getPublishedBlogById(Integer id) {
        return blogRepository.findByIdAndStatus(id, STATUS_PUBLISHED)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài blog hoặc chưa được xuất bản."));
    }

    /**
     * MỚI: Lấy 5 bài viết mới nhất cho Sidebar
     */
    public List<Blog> getLatestPosts() {
        return blogRepository.findTop5ByStatusOrderByUpdatedAtDesc(STATUS_PUBLISHED);
    }

    /**
     * MỚI: Lấy tất cả danh mục đang hoạt động cho Sidebar
     */
    public List<BlogCategory> getAllActiveCategories() {
        return blogCategoryRepository.findByIsActive(true);
    }
}