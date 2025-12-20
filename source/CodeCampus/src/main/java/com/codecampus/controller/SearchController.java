package com.codecampus.controller;

import com.codecampus.entity.Blog;
import com.codecampus.entity.Course;
import com.codecampus.repository.BlogRepository;
import com.codecampus.repository.CourseRepository;
import com.codecampus.repository.MyCourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class SearchController {

    @Autowired private CourseRepository courseRepository;
    @Autowired private BlogRepository blogRepository;
    @Autowired private MyCourseRepository myCourseRepository;

    @GetMapping("/search")
    public String searchGlobal(@RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(defaultValue = "0") int page, // [MỚI] Trang hiện tại (bắt đầu từ 0)
                               @RequestParam(defaultValue = "4") int size, // [MỚI] Số lượng mỗi loại trên 1 trang
                               Model model) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return "redirect:/home";
        }
        List<Course> featuredCourses = courseRepository.findByIsFeaturedAndStatus(true, "ACTIVE");
        featuredCourses.forEach(course -> {
            long studentCount = myCourseRepository.countByCourse_Id(Long.valueOf(course.getId()));
            course.setStudentCount(studentCount);
        });

        String searchTerm = keyword.trim();
        Pageable pageable = PageRequest.of(page, size);

        // 1. Gọi Repository với Pageable
        Page<Course> coursePage = courseRepository.findByNameContainingIgnoreCaseAndStatus(searchTerm, "ACTIVE", pageable);
        Page<Blog> blogPage = blogRepository.findByTitleContainingIgnoreCaseAndStatus(searchTerm, "published", pageable);

        // 2. Logic Thông minh (Smart Redirect) - Chỉ chạy ở trang đầu tiên
        if (page == 0) {
            if (coursePage.getTotalElements() == 1 && blogPage.getTotalElements() == 0) {
                return "redirect:/courses/" + coursePage.getContent().get(0).getId();
            }
            if (blogPage.getTotalElements() == 1 && coursePage.getTotalElements() == 0) {
                return "redirect:/blog/" + blogPage.getContent().get(0).getId();
            }
        }

        // 3. Tính toán dữ liệu cho View
        model.addAttribute("keyword", searchTerm);
        model.addAttribute("coursePage", coursePage); // Truyền Page object
        model.addAttribute("blogPage", blogPage);     // Truyền Page object

        // Tổng kết quả
        long totalResults = coursePage.getTotalElements() + blogPage.getTotalElements();
        model.addAttribute("totalResults", totalResults);

        // Tính tổng số trang lớn nhất (Ví dụ Course có 3 trang, Blog có 5 trang -> Lấy 5)
        int maxPages = Math.max(coursePage.getTotalPages(), blogPage.getTotalPages());
        model.addAttribute("totalPages", maxPages);
        model.addAttribute("currentPage", page);

        return "search-results";
    }
}