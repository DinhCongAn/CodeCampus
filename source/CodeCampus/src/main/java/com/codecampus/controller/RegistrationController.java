package com.codecampus.controller;

import com.codecampus.dto.RegistrationRequest;
import com.codecampus.entity.Course;
import com.codecampus.entity.PricePackage;
import com.codecampus.entity.User;
import com.codecampus.repository.CourseRepository;
import com.codecampus.repository.PricePackageRepository;
import com.codecampus.service.RegistrationService;
import com.codecampus.service.UserService;
import jakarta.servlet.http.HttpServletRequest; // Bắt buộc có để lấy domain động
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegistrationController {

    @Autowired private RegistrationService registrationService;
    @Autowired private UserService userService;
    @Autowired private CourseRepository courseRepository;
    @Autowired private PricePackageRepository pricePackageRepository;

    private User getCurrentUser(Authentication auth) {
        if (auth == null) return null;
        return userService.findUserByEmail(auth.getName());
    }
    // Hàm hỗ trợ lấy đường dẫn domain (Ví dụ: http://localhost:8080)
    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();
        String url = scheme + "://" + serverName;
        if ((scheme.equals("http") && serverPort != 80) || (scheme.equals("https") && serverPort != 443)) {
            url += ":" + serverPort;
        }
        url += contextPath;
        return url;
    }

    // 1. Hiển thị trang Checkout (Giữ nguyên code cũ của bạn)
    @GetMapping("/registration/checkout")
    public String showCheckoutPage(@RequestParam("courseId") Integer courseId,
                                   @RequestParam("packageId") Integer packageId,
                                   @ModelAttribute("errorMessage") String errorMessage,
                                   Model model) {
        try {
            Course course = courseRepository.findById(Long.valueOf(courseId))
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Khóa học"));
            PricePackage pricePackage = pricePackageRepository.findById(packageId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Gói giá"));

            RegistrationRequest request = new RegistrationRequest();
            request.setCourseId(courseId);
            request.setPackageId(packageId);

            model.addAttribute("course", course);
            model.addAttribute("pricePackage", pricePackage);
            model.addAttribute("registrationRequest", request);

            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.addAttribute("errorMessage", errorMessage);
            }
            return "pending-approval"; // View hiển thị thông tin trước khi bấm thanh toán

        } catch (Exception e) {
            return "redirect:/my-courses";
        }
    }

    @PostMapping("/registration/confirm-payment")
    public String handleRegistration(
            @ModelAttribute("registrationRequest") RegistrationRequest request,
            Authentication authentication,
            HttpServletRequest httpServletRequest,
            RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";

        try {
            // 1. Tạo các đường dẫn để Service sử dụng
            String baseUrl = getBaseUrl(httpServletRequest);

            // Link khi thành công (Dù là PayOS trả về hay Miễn phí tự redirect đều dùng link này)
            String successUrl = baseUrl + "/courses/" + request.getCourseId() + "?payment=success";

            // Link khi hủy (chỉ dùng cho PayOS)
            String cancelUrl = baseUrl + "/courses/" + request.getCourseId() + "?payment=cancel";

            // 2. Gọi Service (Service tự quyết định trả về Link PayOS hay Link nội bộ)
            String redirectUrl = registrationService.registerAndGetPaymentUrl(
                    request,
                    currentUser.getEmail(),
                    successUrl,
                    cancelUrl
            );

            // 3. Chuyển hướng
            return "redirect:" + redirectUrl;

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi xử lý: " + e.getMessage());
            // Quay lại trang xác nhận nếu lỗi
            return "redirect:/registration/checkout?courseId=" + request.getCourseId() + "&packageId=" + request.getPackageId();
        }
    }
}