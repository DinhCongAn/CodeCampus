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

    @Value("${app.upload.dir:uploads}")
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
    // --- LOGIC LƯU (ĐÃ SỬA ĐỔI) ---
    public void saveBlog(BlogDto dto, String userEmail, boolean isAdmin) throws IOException {
        Blog blog = new Blog();
        User currentUser = userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));

        boolean isUpdate = dto.getId() != null;
        boolean isOwner = false;

        if (isUpdate) {
            blog = blogRepository.findById(Long.valueOf(dto.getId())).orElseThrow(() -> new RuntimeException("Bài viết không tồn tại"));
            isOwner = blog.getAuthor().getId().equals(currentUser.getId());

            // 1. Nếu không phải Admin VÀ không phải tác giả -> Chặn
            if (!isAdmin && !isOwner) {
                throw new RuntimeException("Bạn không có quyền chỉnh sửa bài viết này!");
            }
        } else {
            // Tạo mới -> Tác giả là người hiện tại
            blog.setAuthor(currentUser);
            isOwner = true; // Tạo mới thì chắc chắn là owner
        }

        // 2. Logic cập nhật dữ liệu
        if (isAdmin && !isOwner) {
            // --- TRƯỜNG HỢP ADMIN DUYỆT BÀI NGƯỜI KHÁC ---
            // Chỉ cho phép update Status, Featured, PublishedAt
            // KHÔNG update Title, Content, Summary, Category, Image

            blog.setStatus(dto.getStatus());
            blog.setIsFeatured(dto.getIsFeatured() != null ? dto.getIsFeatured() : false);
            blog.setPublishedAt(dto.getPublishedAt());

        } else {
            // --- TRƯỜNG HỢP CÒN LẠI (Tác giả tự sửa hoặc Tạo mới) ---
            // Update Full nội dung
            blog.setTitle(dto.getTitle());
            blog.setSummary(dto.getBrief());
            blog.setContent(dto.getContent());

            if (dto.getBlogCategoryId() != null) {
                blog.setCategory(categoryRepository.findById(dto.getBlogCategoryId()).orElse(null));
            }

            // --- XỬ LÝ ẢNH (FILE & LINK) ---

            // Ưu tiên 1: Upload File mới (Nếu người dùng chọn file)
            if (dto.getThumbnail() != null && !dto.getThumbnail().isEmpty()) {
                String fileName = saveFile(dto.getThumbnail());
                // Lưu đường dẫn web (URL) vào DB (map với resource handler)
                blog.setThumbnailUrl("/uploads/blogs/" + fileName);
            }
            // Ưu tiên 2: Lưu Link ảnh (URL)
            // - Nếu người dùng nhập link ảnh online vào tab URL
            // - Hoặc nếu người dùng giữ nguyên ảnh cũ (frontend gửi lại link cũ)
            else if (dto.getThumbnailUrl() != null) {
                // Trim() để xóa khoảng trắng thừa, đảm bảo link sạch
                String url = dto.getThumbnailUrl().trim();
                blog.setThumbnailUrl(url);
            }

            // Logic Status cho User/Admin (khi sửa bài của chính mình)
            if (isAdmin) {
                blog.setStatus(dto.getStatus());
                blog.setIsFeatured(dto.getIsFeatured() != null ? dto.getIsFeatured() : false);
                blog.setPublishedAt(dto.getPublishedAt());
            } else {
                // User thường -> Reset về Draft
                blog.setStatus("draft");
                if (blog.getIsFeatured() == null) blog.setIsFeatured(false);
            }
        }

        blogRepository.save(blog);
    }

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

    private String saveFile(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
        String fileName = UUID.randomUUID().toString() + "_" + StringUtils.cleanPath(file.getOriginalFilename());
        Files.copy(file.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }

    /**
     * Toggle trạng thái Featured của bài viết (Admin)
     */
    public void toggleFeatured(Long blogId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new RuntimeException("Bài viết không tồn tại"));

        // Đảo trạng thái featured
        blog.setIsFeatured(!Boolean.TRUE.equals(blog.getIsFeatured()));

        blogRepository.save(blog);
    }

}