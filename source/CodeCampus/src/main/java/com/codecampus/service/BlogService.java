package com.codecampus.service;

import com.codecampus.dto.BlogDto;
import com.codecampus.entity.Blog;
import com.codecampus.entity.BlogCategory;
import com.codecampus.entity.User;
import com.codecampus.repository.BlogCategoryRepository;
import com.codecampus.repository.BlogRepository;
import com.codecampus.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BlogService {

    private final BlogRepository blogRepository;
    private final BlogCategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.dir:uploads/thumbnails}")
    private String uploadDir;

    private static final String STATUS_PUBLISHED = "published";

    public BlogService(BlogRepository blogRepository, BlogCategoryRepository categoryRepository, UserRepository userRepository) {
        this.blogRepository = blogRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    // ==================================================================
    // PHẦN 1: PUBLIC VIEW (Dành cho người xem - Guest)
    // ==================================================================

    /**
     * Lấy danh sách blog đã xuất bản (Có phân trang, tìm kiếm, lọc)
     */
    public Page<Blog> getPublishedBlogs(int page, int size, String keyword, Integer categoryId) {
        Pageable pageable = PageRequest.of(page, size);
        boolean hasKeyword = (keyword != null && !keyword.trim().isEmpty());
        boolean hasCategory = (categoryId != null && categoryId > 0);

        if (hasKeyword && hasCategory) {
            return blogRepository.findByStatusAndTitleContainingIgnoreCaseAndCategoryIdOrderByUpdatedAtDesc(STATUS_PUBLISHED, keyword, categoryId, pageable);
        } else if (hasKeyword) {
            return blogRepository.findByStatusAndTitleContainingIgnoreCaseOrderByUpdatedAtDesc(STATUS_PUBLISHED, keyword, pageable);
        } else if (hasCategory) {
            return blogRepository.findByStatusAndCategoryIdOrderByUpdatedAtDesc(STATUS_PUBLISHED, categoryId, pageable);
        } else {
            return blogRepository.findByStatusOrderByUpdatedAtDesc(STATUS_PUBLISHED, pageable);
        }
    }

    /**
     * Lấy chi tiết bài blog (Chỉ lấy bài đã published để xem public)
     */
    public Blog getPublishedBlogById(Integer id) {
        return blogRepository.findByIdAndStatus(id, STATUS_PUBLISHED)
                .orElseThrow(() -> new RuntimeException("Bài viết không tồn tại hoặc chưa được xuất bản."));
    }

    /**
     * Widget: Lấy 5 bài viết mới nhất
     */
    public List<Blog> getLatestPosts() {
        return blogRepository.findTop5ByStatusOrderByUpdatedAtDesc(STATUS_PUBLISHED);
    }

    /**
     * Widget: Lấy danh mục đang hoạt động
     */
    public List<BlogCategory> getAllActiveCategories() {
        return categoryRepository.findByIsActive(true);
    }


    // ==================================================================
    // PHẦN 2: QUẢN TRỊ (ADMIN & USER DASHBOARD)
    // ==================================================================

    /**
     * Lấy tất cả danh mục (kể cả ẩn) để đổ vào Dropdown trong trang Admin/User Editor
     */
    public List<BlogCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * Lấy bài viết để chỉnh sửa (Không quan tâm status)
     */
    public Blog getBlogById(Integer id) {
        return blogRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new RuntimeException("Bài viết không tồn tại."));
    }

    /**
     * Danh sách bài viết cho ADMIN (Tìm kiếm, Lọc Status, Phân trang)
     */
    public Page<Blog> getAdminBlogs(String keyword, Integer categoryId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        // Gọi custom query trong Repository
        return blogRepository.searchBlogs(keyword, categoryId, status, pageable);
    }

    /**
     * Danh sách bài viết cho USER (Chỉ lấy bài của chính user đó)
     */
    public List<Blog> findBlogsByAuthor(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        return blogRepository.findByAuthorId(user.getId());
    }

    // ==================================================================
    // PHẦN 3: CRUD LOGIC (TẠO MỚI, CẬP NHẬT, XÓA, UPLOAD)
    // ==================================================================

    /**
     * Lưu bài viết (Xử lý chung cho cả Admin và User)
     */
    public void saveBlog(BlogDto dto, String userEmail, boolean isAdmin) throws IOException {
        Blog blog = new Blog();
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        // 1. Kiểm tra ID để biết là Thêm mới hay Sửa
        if (dto.getId() != null) {
            blog = blogRepository.findById(Long.valueOf(dto.getId())).orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết"));

            // Check quyền: Nếu không phải Admin VÀ không phải tác giả -> Chặn
            if (!isAdmin && !blog.getAuthor().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Bạn không có quyền chỉnh sửa bài viết này!");
            }
        } else {
            // Tạo mới -> Set tác giả
            blog.setAuthor(currentUser);
        }

        // 2. Map dữ liệu từ DTO sang Entity
        blog.setTitle(dto.getTitle());
        blog.setSummary(dto.getBrief()); // Map 'brief' từ form vào 'summary' trong DB
        blog.setContent(dto.getContent());

        if (dto.getBlogCategoryId() != null) {
            BlogCategory category = categoryRepository.findById(dto.getBlogCategoryId()).orElse(null);
            blog.setCategory(category);
        }

        // 3. Phân quyền dữ liệu (Status & Featured)
        if (isAdmin) {
            // Admin có toàn quyền quyết định
            blog.setStatus(dto.getStatus());
            blog.setIsFeatured(dto.getIsFeatured() != null ? dto.getIsFeatured() : false);
            blog.setPublishedAt(dto.getPublishedAt());
        } else {
            // User thường:
            // - Nếu bài mới: Luôn set là DRAFT
            // - Nếu bài cũ: Giữ nguyên hoặc reset về Draft (Ở đây ta reset về Draft để Admin duyệt lại nội dung mới)
            blog.setStatus("draft");

            // User không được quyền set Featured
            if (blog.getIsFeatured() == null) {
                blog.setIsFeatured(false);
            }
        }

        // 4. Xử lý Upload Ảnh (Thumbnail)
        if (dto.getThumbnail() != null && !dto.getThumbnail().isEmpty()) {
            // Nếu có upload ảnh mới
            String fileName = saveFile(dto.getThumbnail());
            blog.setThumbnailUrl("/uploads/" + fileName);
        } else if (dto.getThumbnailUrl() != null) {
            // Nếu không upload, giữ nguyên URL ảnh cũ (từ hidden input)
            blog.setThumbnailUrl(dto.getThumbnailUrl());
        }

        blogRepository.save(blog);
    }

    /**
     * Xóa bài viết
     */
    public void deleteBlog(Integer id, String userEmail, boolean isAdmin) {
        Blog blog = getBlogById(id);

        if (!isAdmin) {
            User currentUser = userRepository.findByEmail(userEmail).orElseThrow();
            if (!blog.getAuthor().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Bạn không có quyền xóa bài viết này!");
            }
        }
        blogRepository.delete(blog);
    }

    /**
     * Helper: Lưu file vào thư mục server
     */
    private String saveFile(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Tạo tên file ngẫu nhiên để tránh trùng lặp
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = "";
        int i = originalFileName.lastIndexOf('.');
        if (i > 0) {
            extension = originalFileName.substring(i);
        }
        String fileName = UUID.randomUUID().toString() + extension;

        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }
}