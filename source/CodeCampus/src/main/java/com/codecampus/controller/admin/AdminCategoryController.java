package com.codecampus.controller.admin;

import com.codecampus.entity.CourseCategory;
import com.codecampus.service.CategoryService;
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

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminCategoryController {

    @Autowired private CategoryService categoryService;

    // 1. Danh sách
    @GetMapping("/categories")
    public String showCategories(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Boolean status, // true/false/null
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "7") int size,
            Model model) {

        if (keyword != null && keyword.trim().isEmpty()) keyword = null;

        Page<CourseCategory> categoryPage = categoryService.getCategoriesAdmin(keyword, status, page, size);

        model.addAttribute("categoryPage", categoryPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("activePage", "categories");

        return "admin/categories";
    }

    // 2. API Chi tiết (Cho Modal)
    @GetMapping("/categories/api/{id}")
    @ResponseBody
    public ResponseEntity<?> getCategoryApi(@PathVariable Integer id) {
        try {
            CourseCategory cat = categoryService.getCategoryById(id);
            Map<String, Object> data = new HashMap<>();
            data.put("id", cat.getId());
            data.put("name", cat.getName());
            data.put("description", cat.getDescription());
            data.put("isActive", cat.getIsActive());
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 3. Lưu
    @PostMapping("/categories/save")
    public String saveCategory(@Valid @ModelAttribute CourseCategory category,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            String msg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + msg);
            return "redirect:/admin/categories";
        }

        try {
            categoryService.saveCategory(category);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu danh mục thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    // 4. Toggle Status
    @PostMapping("/categories/toggle/{id}")
    public String toggleStatus(@PathVariable Integer id, RedirectAttributes redirectAttributes,
                               HttpServletRequest request) {
        try {
            categoryService.toggleStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/admin/categories");
    }
}