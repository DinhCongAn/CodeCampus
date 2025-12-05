package com.codecampus.controller;

import com.codecampus.dto.BlogDto;
import com.codecampus.entity.Blog;
import com.codecampus.service.BlogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user/my-blogs")
public class UserBlogController {

    private final BlogService blogService;

    public UserBlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    // 1. Hiển thị danh sách & Modal HTML
    @GetMapping("")
    public String listMyBlogs(Model model, Authentication auth) {
        // Lấy danh sách bài viết của chính User đang đăng nhập
        model.addAttribute("blogs", blogService.findBlogsByAuthor(auth.getName()));
        model.addAttribute("categories", blogService.getAllCategories()); // Để đổ vào dropdown trong Modal
        return "user/blog/list";
    }

    // 2. API JSON: Lấy chi tiết 1 bài viết để đổ vào Modal Sửa
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> getBlogApi(@PathVariable Integer id, Authentication auth) {
        try {
            Blog blog = blogService.getBlogById(id);

            // BẢO MẬT: Kiểm tra xem bài viết có phải của người này không
            if (!blog.getAuthor().getEmail().equals(auth.getName())) {
                return ResponseEntity.status(403).body("Bạn không có quyền truy cập bài viết này");
            }

            return ResponseEntity.ok(mapToDto(blog));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 3. Xử lý Lưu (Thêm mới / Cập nhật)
    @PostMapping("/save")
    public String saveBlog(@ModelAttribute BlogDto dto,
                           Authentication auth,
                           RedirectAttributes ra) {
        try {
            // isAdmin = false -> Service sẽ tự động set Status = DRAFT và check quyền sở hữu
            blogService.saveBlog(dto, auth.getName(), false);
            ra.addFlashAttribute("successMessage", "Bài viết đã được lưu thành công! Vui lòng chờ Admin phê duyệt.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/user/my-blogs";
    }

    // 4. Xử lý Xóa
    @GetMapping("/delete/{id}")
    public String deleteBlog(@PathVariable Integer id,
                             Authentication auth,
                             RedirectAttributes ra) {
        try {
            blogService.deleteBlog(id, auth.getName(), false); // false = not Admin
            ra.addFlashAttribute("successMessage", "Đã xóa bài viết.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/user/my-blogs";
    }

    // Helper mapping
    private BlogDto mapToDto(Blog blog) {
        BlogDto dto = new BlogDto();
        dto.setId(blog.getId());
        dto.setTitle(blog.getTitle());
        dto.setBrief(blog.getSummary());
        dto.setContent(blog.getContent());
        dto.setThumbnailUrl(blog.getThumbnailUrl());
        // User không cần quan tâm status hay featured khi load lên để sửa (vì bị ẩn)
        if(blog.getCategory() != null) dto.setBlogCategoryId(blog.getCategory().getId());
        return dto;
    }
}