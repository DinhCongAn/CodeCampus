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
import org.springframework.web.bind.annotation.*;
// Bỏ: @PathVariable
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
                                   @ModelAttribute("errorMessage") String errorMessage,
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

            // BỔ SUNG: Đẩy lỗi ra view (nếu có)
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.addAttribute("errorMessage", errorMessage);
            }
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
            @ModelAttribute("registrationRequest") RegistrationRequest request,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";

        try {
            // Hàm service (đã sửa) giờ sẽ văng lỗi nếu PENDING bị trùng
            registrationService.createPendingRegistrationAndSendEmail(
                    request,
                    currentUser.getEmail()
            );

            // Gửi thông báo về trang "Khóa học của tôi"
            redirectAttributes.addFlashAttribute("successMessage",
                    "Bạn đã đăng ký thành công! Chúng tôi sẽ xử lý trong giây lát.");

            return "redirect:/my-courses"; // Chuyển hướng về Khóa học của tôi

        } catch (Exception e) {
            // Gửi thông báo lỗi
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

            // Chuyển hướng (redirect) NGƯỢC LẠI trang checkout (trang QR)
            // Kèm theo courseId và packageId để trang đó load lại
            return "redirect:/registration/checkout?courseId="
                    + request.getCourseId() + "&packageId=" + request.getPackageId();
            // =========================
        }
    }

    // BỔ SUNG TÍNH NĂNG HỦY (CANCEL)
    // ==========================================================
    @PostMapping("/registration/cancel/{id}")
    public String cancelRegistration(@PathVariable("id") Integer registrationId,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";

        try {
            // Gọi service để hủy
            registrationService.cancelRegistration(registrationId, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn đăng ký thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hủy đăng ký: " + e.getMessage());
        }

        return "redirect:/my-courses";
    }
    /**
     * 3. Hiển thị lại trang "Chờ duyệt" (Khi bấm "Xem thanh toán" ở /my-courses)
     */
    @GetMapping("/registration/pending/{orderCode}")
    public String showPendingPage(@PathVariable("orderCode") String orderCode, Model model) {
        try {
            // Lấy đơn hàng (ĐÃ TỒN TẠI)
            Registration reg = registrationService.getRegistrationByOrderCode(orderCode);

            // Tạo một DTO rỗng (chỉ để form không bị lỗi)
            RegistrationRequest request = new RegistrationRequest();
            request.setCourseId(reg.getCourse().getId());
            request.setPackageId(reg.getPricePackage().getId());

            // Gửi dữ liệu ra (lấy từ đơn hàng đã lưu)
            model.addAttribute("course", reg.getCourse());
            model.addAttribute("pricePackage", reg.getPricePackage());
            model.addAttribute("registrationRequest", request); // DTO

            // SỬA: Thêm chính 'registration' để lấy orderCode
            model.addAttribute("registration", reg);

            return "pending-approval"; // Trả về trang QR

        } catch (Exception e) {
            return "redirect:/my-courses";
        }
    }
}