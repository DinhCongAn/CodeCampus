package com.codecampus.controller.admin;

import com.codecampus.entity.Course;
import com.codecampus.repository.CourseCategoryRepository;
import com.codecampus.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminSubjectController {

    @Autowired private CourseService courseService;
    @Autowired private CourseCategoryRepository categoryRepository;

    // 1. Hiển thị danh sách (View chính)
    @GetMapping("/subjects")
    public String showSubjects(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        // Nếu keyword rỗng thì đưa về null (để query gọn hơn, tùy chọn)
        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }

        // Nếu status là chuỗi rỗng "" (do chọn dòng --Tất cả--), ép nó về null
        if (status != null && status.trim().isEmpty()) {
            status = null;
        }
        Page<Course> coursePage = courseService.getCoursesForAdmin(keyword, categoryId, status, page, size);

        model.addAttribute("coursePage", coursePage);
        model.addAttribute("categories", categoryRepository.findAll()); // Load dropdown danh mục

        // Giữ lại bộ lọc
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("status", status);
        model.addAttribute("activePage", "subjects");

        return "admin/subjects";
    }

    // 2. API lấy chi tiết (SỬA LẠI ĐỂ TRÁNH LỖI JSON)
    @GetMapping("/subjects/api/{id}")
    @ResponseBody
    public ResponseEntity<?> getCourseApi(@PathVariable Long id) {
        try {
            Course course = courseService.getCourseById(id);

            // Tạo Map thủ công để kiểm soát dữ liệu trả về
            Map<String, Object> data = new HashMap<>();
            data.put("id", course.getId());
            data.put("name", course.getName());
            data.put("thumbnailUrl", course.getThumbnailUrl());
            data.put("description", course.getDescription()); // Dùng description thay tagline
            data.put("status", course.getStatus());

            // Lấy ID danh mục an toàn (tránh NullPointer)
            if (course.getCategory() != null) {
                data.put("categoryId", course.getCategory().getId());
            } else {
                data.put("categoryId", "");
            }

            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 3. Lưu (Thêm mới hoặc Edit)
    @PostMapping("/subjects/save")
    public String saveCourse(@ModelAttribute Course course, RedirectAttributes redirectAttributes) {
        try {
            courseService.saveCourse(course);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu thông tin môn học thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/subjects";
    }

    // 4. Đổi trạng thái nhanh (Ẩn/Hiện)
    @PostMapping("/subjects/toggle/{id}")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            courseService.toggleCourseStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/subjects";
    }
}