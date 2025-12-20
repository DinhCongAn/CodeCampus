package com.codecampus.controller.admin;

import com.codecampus.entity.Slider;
import com.codecampus.service.SliderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminSliderController {

    @Autowired private SliderService sliderService;

    // 1. Danh sách
    @GetMapping("/sliders")
    public String showSliders(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "7") int size,
            Model model) {

        if (keyword != null && keyword.trim().isEmpty()) keyword = null;
        if (status != null && status.trim().isEmpty()) status = null;

        Page<Slider> sliderPage = sliderService.getSlidersAdmin(keyword, status, page, size);

        model.addAttribute("sliderPage", sliderPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("activePage", "sliders"); // Active Sidebar

        return "admin/sliders";
    }

    // 2. API Chi tiết (Cho Modal)
    @GetMapping("/sliders/api/{id}")
    @ResponseBody
    public ResponseEntity<?> getSliderApi(@PathVariable Integer id) {
        try {
            Slider slider = sliderService.getSliderById(id);
            return ResponseEntity.ok(slider); // Trả về JSON
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 3. Lưu
    @PostMapping("/sliders/save")
    public String saveSlider(@Valid @ModelAttribute Slider slider,
                             BindingResult bindingResult,
                             @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                             RedirectAttributes redirectAttributes) {

        try {
            // A. Validate cơ bản (Tiêu đề rỗng...)
            if (bindingResult.hasErrors()) {
                String msg = bindingResult.getAllErrors().get(0).getDefaultMessage();
                throw new RuntimeException(msg);
            }

            // B. Validate Ảnh (Logic quan trọng)
            boolean hasNewFile = (imageFile != null && !imageFile.isEmpty());
            boolean hasUrl = (slider.getImageUrl() != null && !slider.getImageUrl().trim().isEmpty());

            // Trường hợp Thêm mới: Bắt buộc phải có File HOẶC URL
            if (slider.getId() == null) {
                if (!hasNewFile && !hasUrl) {
                    throw new RuntimeException("Vui lòng chọn ảnh tải lên hoặc nhập đường dẫn ảnh.");
                }
            }
            // Trường hợp Sửa:
            else {
                // Nếu không có file mới, và url bị xóa rỗng -> Lấy lại ảnh cũ từ DB
                if (!hasNewFile && !hasUrl) {
                    Slider oldSlider = sliderService.getSliderById(slider.getId());
                    slider.setImageUrl(oldSlider.getImageUrl());
                }
            }

            // C. Gọi Service lưu
            sliderService.saveSlider(slider, imageFile);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu Slider thành công!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/sliders";
    }

    // 4. Toggle Status
    @PostMapping("/sliders/toggle/{id}")
    public String toggleStatus(@PathVariable Integer id, RedirectAttributes redirectAttributes,
                               HttpServletRequest request) {
        try {
            sliderService.toggleStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/admin/sliders");
    }
}