// src/main/java/com/codecampus/controller/RegistrationController.java
package com.codecampus.controller;

import com.codecampus.dto.RegistrationRequest;
import com.codecampus.entity.Course; // BỔ SUNG
import com.codecampus.entity.PricePackage; // BỔ SUNG
import com.codecampus.entity.Registration;
import com.codecampus.entity.User;
import com.codecampus.repository.CourseRepository; // BỔ SUNG
import com.codecampus.repository.PricePackageRepository; // BỔ SUNG
import com.codecampus.service.RegistrationService;
import com.codecampus.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
// Bỏ: @PathVariable
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam; // BỔ SUNG
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegistrationController {

    @Autowired private RegistrationService registrationService;
    @Autowired private UserService userService;
    @Autowired private CourseRepository courseRepository; // BỔ SUNG
    @Autowired private PricePackageRepository pricePackageRepository; // BỔ SUNG

    private User getCurrentUser(Authentication auth) {
        if (auth == null) return null;
        return userService.findUserByEmail(auth.getName());
    }

    /**
     * (MỚI) 1. Hiển thị trang thanh toán (KHÔNG LƯU DB)
     * Được gọi bởi Pop-up (method="GET")
     */
    @GetMapping("/registration/checkout")
    public String showCheckoutPage(@RequestParam("courseId") Integer courseId,
                                   @RequestParam("packageId") Integer packageId,
                                   Model model) {
        try {
            // Lấy thông tin (chưa lưu) để hiển thị
            Course course = courseRepository.findById(Long.valueOf(courseId))
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Khóa học"));
            PricePackage pricePackage = pricePackageRepository.findById(packageId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Gói giá"));

            // DTO này sẽ được dùng để POST đi ở bước sau
            RegistrationRequest request = new RegistrationRequest();
            request.setCourseId(courseId);
            request.setPackageId(packageId);

            model.addAttribute("course", course);
            model.addAttribute("pricePackage", pricePackage);
            model.addAttribute("registrationRequest", request); // Gửi DTO rỗng

            return "pending-approval"; // Trả về trang thanh toán (QR cố định)

        } catch (Exception e) {
            return "redirect:/my-courses";
        }
    }

    /**
     * (MỚI) 2. Xử lý "Tôi đã chuyển khoản" (LƯU DB + GỬI MAIL)
     * Đây là nơi thực sự tạo đơn hàng
     */
    @PostMapping("/registration/confirm-payment")
    public String handleRegistration(
            @ModelAttribute RegistrationRequest request, // DTO (chỉ có 2 trường)
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";

        try {
            // Hàm này (sửa ở bước 3) sẽ VỪA LƯU DB, VỪA GỬI MAIL
            registrationService.createPendingRegistrationAndSendEmail(
                    request,
                    currentUser.getEmail()
            );

            // Gửi thông báo về trang "Khóa học của tôi"
            redirectAttributes.addFlashAttribute("successMessage",
                    "Bạn đã đăng ký thành công! Chúng tôi sẽ xử lý trong giây lát.");

            return "redirect:/my-courses"; // Chuyển hướng về Khóa học của tôi

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/courses/" + request.getCourseId();
        }
    }

}