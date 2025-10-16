package com.codecampus.controller;

import com.codecampus.entity.Blog;
import com.codecampus.entity.Course;
import com.codecampus.repository.BlogRepository;
import com.codecampus.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {
    // Inject thẳng các Repository cần thiết để lấy dữ liệu
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private BlogRepository blogRepository;

    @GetMapping("/home") // Xử lý request đến trang chủ (http://localhost:8080/)
    public String showHomePage(Model model) {

        // --- LẤY DỮ LIỆU TỪ DATABASE ---

        // 1. Lấy danh sách các khóa học nổi bật (is_featured = 1)
        List<Course> featuredCourses = courseRepository.findAll();

        // 2. Lấy 3 bài blog mới nhất đã được xuất bản
        List<Blog> latestBlogs = blogRepository.findAll();

        // --- GỬI DỮ LIỆU RA VIEW ---

        // 3. Thêm các danh sách dữ liệu vào đối tượng "Model"
        // Tên "featuredCourses" và "latestBlogs" phải khớp với tên trong file home.html
        model.addAttribute("featuredCourses", featuredCourses);
        model.addAttribute("latestBlogs", latestBlogs);

        // 4. Trả về tên của file view (Thymeleaf sẽ tìm file home.html)
        return "home";
    }
}
