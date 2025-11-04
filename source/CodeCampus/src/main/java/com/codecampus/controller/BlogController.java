package com.codecampus.controller;

import com.codecampus.entity.Blog;
import com.codecampus.service.BlogService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BlogController {

    private final BlogService blogService;

    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    /**
     * Trang 1: Hiển thị DANH SÁCH BLOG (có phân trang)
     */
    @GetMapping("/blog")
    public String showBlogList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            Model model) {

        Page<Blog> blogPage = blogService.getPublishedBlogs(page, size);

        model.addAttribute("blogPage", blogPage); // Gửi đối tượng Page sang view

        return "blog-list"; // Trả về templates/blog-list.html
    }

    /**
     * Trang 2: Hiển thị CHI TIẾT BLOG
     */
    @GetMapping("/blog/{id}")
    public String showBlogDetails(@PathVariable("id") Integer id, Model model) {
        try {
            Blog blog = blogService.getPublishedBlogById(id);
            model.addAttribute("blog", blog);
            return "blog-details"; // Trả về templates/blog-details.html
        } catch (RuntimeException e) {
            // Xử lý nếu không tìm thấy blog
            return "redirect:/blog?error=notFound";
        }
    }
}