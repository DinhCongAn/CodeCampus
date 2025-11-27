package com.codecampus.controller.admin;

import com.codecampus.entity.PricePackage;
import com.codecampus.service.CourseService;
import com.codecampus.service.PricePackageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/price-packages")
public class AdminPricePackageController {

    @Autowired private PricePackageService pricePackageService;
    @Autowired private CourseService courseService;

    // 1. Hiển thị danh sách gói giá của 1 khóa học
    @GetMapping
    public String showPackages(@RequestParam("courseId") Long courseId, Model model) {
        model.addAttribute("course", courseService.getCourseById(courseId)); // Lấy info khóa học để hiện tên
        model.addAttribute("packages", pricePackageService.getPackagesByCourse(courseId));
        model.addAttribute("courseId", courseId); // Để truyền vào form thêm mới
        return "admin/price-packages";
    }

    // 2. API lấy chi tiết (Cho Modal)
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> getPackageApi(@PathVariable Integer id) {
        try {
            PricePackage pkg = pricePackageService.getPackageById(id);

            // Tạo Map thủ công để kiểm soát dữ liệu, tránh Jackson quét vào các object Lazy
            Map<String, Object> data = new HashMap<>();
            data.put("id", pkg.getId());
            data.put("name", pkg.getName());
            data.put("durationMonths", pkg.getDurationMonths());
            data.put("listPrice", pkg.getListPrice());
            data.put("salePrice", pkg.getSalePrice());
            data.put("description", pkg.getDescription());
            data.put("status", pkg.getStatus());

            // Không put object "course" vào đây để cắt đứt chuỗi quan hệ gây lỗi

            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 3. Lưu (Chỉ Admin mới được gọi - Sẽ check role ở View hoặc Security Config)
    @PostMapping("/save")
    public String savePackage(@Valid @ModelAttribute PricePackage pricePackage,
                              BindingResult bindingResult,
                              @RequestParam("courseId") Long courseId,
                              RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi nhập liệu: " + bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/admin/price-packages?courseId=" + courseId;
        }

        try {
            pricePackageService.savePackage(pricePackage, courseId);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu gói giá thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        return "redirect:/admin/price-packages?courseId=" + courseId;
    }

    // 4. Xóa
    @PostMapping("/delete/{id}")
    public String deletePackage(@PathVariable Integer id,
                                @RequestParam("courseId") Long courseId,
                                RedirectAttributes redirectAttributes) {
        try {
            pricePackageService.deletePackage(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa gói giá.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa: " + e.getMessage());
        }
        return "redirect:/admin/price-packages?courseId=" + courseId;
    }
}