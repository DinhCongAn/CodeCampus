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
     * CẬP NHẬT: Hỗ trợ tìm kiếm theo cả keyword và categoryId
     */
    public Page<Blog> getPublishedBlogs(int page, int size, String keyword, Integer categoryId) {
        Pageable pageable = PageRequest.of(page, size);

        // Kiểm tra xem có giá trị tìm kiếm/lọc không
        boolean hasKeyword = (keyword != null && !keyword.trim().isEmpty());
        // Giả sử 0 hoặc null nghĩa là "Tất cả danh mục"
        boolean hasCategory = (categoryId != null && categoryId > 0);

        if (hasKeyword && hasCategory) {
            // 1. Tìm theo cả Keyword VÀ Category
            return blogRepository.findByStatusAndTitleContainingIgnoreCaseAndCategoryIdOrderByUpdatedAtDesc(STATUS_PUBLISHED, keyword, categoryId, pageable);
        } else if (hasKeyword) {
            // 2. Chỉ tìm theo Keyword
            return blogRepository.findByStatusAndTitleContainingIgnoreCaseOrderByUpdatedAtDesc(STATUS_PUBLISHED, keyword, pageable);
        } else if (hasCategory) {
            // 3. Chỉ lọc theo Category
            return blogRepository.findByStatusAndCategoryIdOrderByUpdatedAtDesc(STATUS_PUBLISHED, categoryId, pageable);
        } else {
            // 4. Mặc định (không tìm, không lọc)
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