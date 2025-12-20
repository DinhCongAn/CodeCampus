package com.codecampus.controller.admin;

import com.codecampus.entity.Course;
import com.codecampus.entity.User;
import com.codecampus.repository.CourseCategoryRepository;
import com.codecampus.repository.UserRepository;
import com.codecampus.service.CourseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminSubjectController {

    @Autowired private CourseService courseService;
    @Autowired private CourseCategoryRepository categoryRepository;
    @Autowired private UserRepository userRepository;

    /**
     * 1. HIỂN THỊ DANH SÁCH MÔN HỌC (Kèm bộ lọc)
     */
    @GetMapping("/subjects")
    public String showSubjects(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "7") int size,
            Model model) {

        // Xử lý chuỗi rỗng thành null cho bộ lọc hoạt động đúng
        if (keyword != null && keyword.trim().isEmpty()) keyword = null;
        if (status != null && status.trim().isEmpty()) status = null;

        // Gọi Service lấy dữ liệu phân trang
        Page<Course> coursePage = courseService.getCoursesForAdmin(keyword, categoryId, status, page, size);

        // Đẩy dữ liệu ra View
        model.addAttribute("coursePage", coursePage);

        // Load danh sách danh mục cho Dropdown
        model.addAttribute("categories", categoryRepository.findAll());

        // Load danh sách Owner (Lấy tất cả user là admin hoặc teacher)

        List<String> targetRoles = Arrays.asList("ADMIN", "TEACHER");
        List<User> owners = userRepository.findByRoleNames(targetRoles);
        model.addAttribute("owners", owners);

        // Giữ lại giá trị lọc để hiển thị trên giao diện
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("status", status);
        model.addAttribute("activePage", "subjects"); // Để Active menu Sidebar

        return "admin/subjects";
    }

    /**
     * 2. API LẤY CHI TIẾT (Phục vụ Modal Edit)
     * Trả về Map thay vì Entity để tránh lỗi Hibernate Proxy/Lazy Loading
     */
    @GetMapping("/subjects/api/{id}")
    @ResponseBody
    public ResponseEntity<?> getCourseApi(@PathVariable Long id) {
        try {
            Course course = courseService.getCourseById(id);

            // Tạo Map thủ công để kiểm soát dữ liệu JSON trả về
            Map<String, Object> data = new HashMap<>();
            data.put("id", course.getId());
            data.put("name", course.getName());
            data.put("thumbnailUrl", course.getThumbnailUrl());
            data.put("description", course.getDescription()); // Dùng description thay tagline
            data.put("status", course.getStatus());
            data.put("isFeatured", course.getIsFeatured()); // Trả về trạng thái Nổi bật (true/false)

            // Lấy ID danh mục an toàn
            if (course.getCategory() != null) {
                data.put("categoryId", course.getCategory().getId());
            } else {
                data.put("categoryId", "");
            }

            // Lấy ID Owner an toàn
            if (course.getOwner() != null) {
                data.put("ownerId", course.getOwner().getId());
            } else {
                data.put("ownerId", "");
            }

            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 3. LƯU MÔN HỌC (Thêm mới hoặc Cập nhật)
     * Có Validate dữ liệu đầu vào (@Valid)
     */
    @PostMapping("/subjects/save")
    public String saveCourse(@Valid @ModelAttribute Course course,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {

        // --- BƯỚC 1: KIỂM TRA VALIDATE CƠ BẢN (Rỗng, độ dài...) ---
        if (bindingResult.hasErrors()) {
            // Lấy lỗi đầu tiên để hiển thị cho gọn
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi nhập liệu: " + errorMsg);
            return "redirect:/admin/subjects";
        }

        // --- BƯỚC 2: GỌI SERVICE LƯU (Có check trùng tên trong Service) ---
        try {
            courseService.saveCourse(course);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu thông tin môn học thành công!");
        } catch (Exception e) {
            // Bắt lỗi logic (Ví dụ: Trùng tên môn học) từ Service ném ra
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        return "redirect:/admin/subjects";
    }

    /**
     * 4. ĐỔI TRẠNG THÁI NHANH (Ẩn / Hiện)
     */
    @PostMapping("/subjects/toggle/{id}")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes,
                               HttpServletRequest request) {
        try {
            courseService.toggleCourseStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái hiển thị.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật: " + e.getMessage());
        }
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/admin/subjects");
    }
}