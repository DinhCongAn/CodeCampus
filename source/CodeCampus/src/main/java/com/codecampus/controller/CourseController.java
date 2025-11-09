package com.codecampus.controller;

import com.codecampus.entity.Course;
import com.codecampus.entity.CourseCategory;
import com.codecampus.entity.PricePackage; // BỔ SUNG
import com.codecampus.repository.UserRepository;
import com.codecampus.service.CourseService;
import com.codecampus.service.PricePackageService; // BỔ SUNG
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class CourseController {

    private final CourseService courseService;
    private final PricePackageService pricePackageService; // BỔ SUNG
    private final UserRepository userRepository; // BỔ SUNG

    // BỔ SUNG PricePackageService vào constructor
    public CourseController(CourseService courseService, PricePackageService pricePackageService,UserRepository userRepository) {
        this.courseService = courseService;
        this.pricePackageService = pricePackageService; // BỔ SUNG
        this.userRepository = userRepository; // BỔ SUNG
    }

    /**
     * Phương thức chung để tải dữ liệu Sidebar
     */
    private void loadSidebarData(Model model) {
        List<CourseCategory> categories = courseService.getAllActiveCategories();
        List<Course> featuredCourses = courseService.getFeaturedCourses();
        model.addAttribute("courseCategories", categories);
        model.addAttribute("featuredCourses", featuredCourses);
    }

    /**
     * Màn hình 10: Hiển thị DANH SÁCH KHÓA HỌC (List)
     */
    @GetMapping("/courses")
    public String showCourseList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            Model model) {

        loadSidebarData(model);
        Page<Course> coursePage = courseService.getPublishedCourses(page, size, keyword, categoryId);
        model.addAttribute("coursePage", coursePage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("courseService", courseService);

        return "course-list";
    }

    /**
     * Màn hình 11: Hiển thị CHI TIẾT KHÓA HỌC (Details)
     */
    @GetMapping("/courses/{id}")
    public String showCourseDetails(@PathVariable("id") Integer id, Model model) {
        try {
            loadSidebarData(model);
            Course course = courseService.getPublishedCourseById(id);
            model.addAttribute("course", course);
            model.addAttribute("lowestPriceOpt", courseService.getLowestPrice(id));

            // ===== BẮT BUỘC BỔ SUNG DÒNG NÀY =====
            // Lấy danh sách gói giá để truyền ra Modal
            List<PricePackage> packages = pricePackageService.getPackagesByCourseId(id);
            model.addAttribute("pricePackages", packages);
            // ===================================


            return "course-details"; // Trả về file course-details.html
        } catch (RuntimeException e) {
            return "redirect:/courses?error=notFound";
        }
    }
}