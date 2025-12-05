package com.codecampus.controller.admin;

import com.codecampus.dto.BlogDto;
import com.codecampus.entity.Blog;
import com.codecampus.service.BlogService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/blogs")
public class AdminBlogController {

    private final BlogService blogService;

    public AdminBlogController(BlogService blogService) { this.blogService = blogService; }

    // 1. Danh sách (CÓ LỌC & PHÂN TRANG)
    @GetMapping("")
    public String list(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Integer categoryId,
                       @RequestParam(required = false) String status) {

        // Gọi Service lấy Page
        Page<Blog> blogPage = blogService.getAdminBlogs(keyword, categoryId, status, page, size);

        model.addAttribute("blogPage", blogPage); // Đổi tên biến thành blogPage để khớp logic phân trang
        model.addAttribute("categories", blogService.getAllCategories()); // QUAN TRỌNG: Để đổ vào dropdown lọc

        // Giữ lại giá trị filter để hiển thị trên UI
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("status", status);
        model.addAttribute("isAdmin", true);

        return "admin/blogs";
    }

    // API JSON cho Modal Edit
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<BlogDto> getBlogApi(@PathVariable Integer id) {
        try {
            Blog blog = blogService.getBlogById(id);
            BlogDto dto = new BlogDto();
            dto.setId(blog.getId());
            dto.setTitle(blog.getTitle());
            dto.setBrief(blog.getSummary());
            dto.setContent(blog.getContent());
            dto.setStatus(blog.getStatus());
            dto.setIsFeatured(blog.getIsFeatured());
            dto.setThumbnailUrl(blog.getThumbnailUrl());
            dto.setPublishedAt(blog.getPublishedAt());
            if(blog.getCategory() != null) dto.setBlogCategoryId(blog.getCategory().getId());
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/save")
    public String save(@ModelAttribute BlogDto dto, Authentication auth, RedirectAttributes ra) {
        try {
            blogService.saveBlog(dto, auth.getName(), true);
            ra.addFlashAttribute("successMessage", "Lưu bài viết thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/blogs";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, Authentication auth, RedirectAttributes ra) {
        try {
            blogService.deleteBlog(id, auth.getName(), true);
            ra.addFlashAttribute("successMessage", "Xóa thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/blogs";
    }
}