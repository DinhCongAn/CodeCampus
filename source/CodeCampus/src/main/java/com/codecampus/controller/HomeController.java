package com.codecampus.controller;

import com.codecampus.entity.Blog;
import com.codecampus.entity.Course;
import com.codecampus.entity.Setting;
import com.codecampus.entity.Slider;
import com.codecampus.repository.BlogRepository;
import com.codecampus.repository.CourseRepository;
import com.codecampus.repository.SliderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    // Inject tất cả các Repository cần thiết để lấy dữ liệu
    @Autowired private SliderRepository sliderRepository;
    @Autowired private BlogRepository blogRepository;
    @Autowired private CourseRepository courseRepository;

    @GetMapping("/home")
    public String showHomePage(Model model) {

        // 1. Lấy dữ liệu cho Sliders
        // Lấy tất cả các slider đang có trạng thái "active"
        List<Slider> activeSliders = sliderRepository.findByStatus("active");
        model.addAttribute("sliders", activeSliders);

        // 2. Lấy dữ liệu cho Hot Posts (Bài viết nổi bật)
        // Giả sử "hot posts" là 4 bài viết mới nhất đã được publish
        List<Blog> hotPosts = blogRepository.findTop4ByStatusOrderByPublishedAtDesc("published");
        model.addAttribute("hotPosts", hotPosts);

        // 3. Lấy dữ liệu cho Featured Subjects (Khóa học nổi bật)
        // Lấy tất cả các khóa học có cờ is_featured = true và đã publish
        List<Course> featuredCourses = courseRepository.findByIsFeaturedAndStatus(true, "ACTIVE");
        model.addAttribute("featuredCourses", featuredCourses);

        // 4. Lấy dữ liệu cho Sidebar with latest posts
        // Giả sử sidebar hiển thị 5 bài viết mới nhất
        List<Blog> latestPostsForSidebar = blogRepository.findTop5ByStatusOrderByPublishedAtDesc("published");
        model.addAttribute("latestPostsForSidebar", latestPostsForSidebar);

        // Trả về tên của file view là "home.html"
        return "home";
    }
}
