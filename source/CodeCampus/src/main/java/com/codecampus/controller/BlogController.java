package com.codecampus.controller;

import com.codecampus.entity.Blog;
import com.codecampus.entity.BlogCategory; // Thêm import
import com.codecampus.service.BlogService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List; // Thêm import

@Controller
public class BlogController {

    private final BlogService blogService;

    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    /**
     * CẬP NHẬT: Trang Danh sách Blog (List)
     */
    @GetMapping("/blog")
    public String showBlogList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(required = false) String keyword, // Thêm param tìm kiếm
            Model model) {

        // 1. Lấy danh sách bài viết (đã phân trang và tìm kiếm)
        Page<Blog> blogPage = blogService.getPublishedBlogs(page, size, keyword);

        // 2. Lấy dữ liệu cho Sidebar
        List<BlogCategory> categories = blogService.getAllActiveCategories();
        List<Blog> latestPosts = blogService.getLatestPosts();

        // 3. Gửi dữ liệu sang View
        model.addAttribute("blogPage", blogPage);
        model.addAttribute("categories", categories);
        model.addAttribute("latestPosts", latestPosts);
        model.addAttribute("keyword", keyword); // Gửi lại từ khóa để giữ trên ô search

        return "blog-list"; // Trả về templates/blog-list.html
    }

    /**
     * CẬP NHẬT: Trang Chi tiết Blog (Details)
     */
    @GetMapping("/blog/{id}")
    public String showBlogDetails(@PathVariable("id") Integer id, Model model) {
        try {
            // 1. Lấy chi tiết bài viết
            Blog blog = blogService.getPublishedBlogById(id);

            // 2. Lấy dữ liệu cho Sidebar
            List<BlogCategory> categories = blogService.getAllActiveCategories();
            List<Blog> latestPosts = blogService.getLatestPosts();

            // 3. Gửi dữ liệu sang View
            model.addAttribute("blog", blog);
            model.addAttribute("categories", categories);
            model.addAttribute("latestPosts", latestPosts);

            return "blog-details"; // Trả về templates/blog-details.html
        } catch (RuntimeException e) {
            return "redirect:/blog?error=notFound";
        }
    }
}