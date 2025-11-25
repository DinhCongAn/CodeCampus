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

    /**
     * [THAY ĐỔI LỚN] Xử lý nút "Thanh toán" -> Chuyển sang PayOS
     */
    @PostMapping("/registration/confirm-payment")
    public String handleRegistration(
            @ModelAttribute("registrationRequest") RegistrationRequest request,
            Authentication authentication,
            HttpServletRequest httpServletRequest, // Thêm tham số này
            RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";

        try {
            // 1. Tạo các URL để PayOS biết đường quay về
            String baseUrl = getBaseUrl(httpServletRequest);
            // Nếu thành công -> về trang khóa học của tôi
            String successUrl = baseUrl + "/my-courses?payment=success";
            // Nếu hủy -> quay lại trang chi tiết khóa học
            String cancelUrl = baseUrl + "/courses/" + request.getCourseId() + "?payment=cancel";
            // 2. Gọi Service để tạo đơn hàng PENDING và lấy Link PayOS
            // (Hàm này chúng ta vừa thêm ở bước trước)
            String checkoutUrl = registrationService.registerAndGetPaymentUrl(
                    request,
                    currentUser.getEmail(),
                    successUrl,
                    cancelUrl
            );

            // 3. CHUYỂN HƯỚNG NGƯỜI DÙNG SANG PAYOS
            return "redirect:" + checkoutUrl;

        } catch (Exception e) {
            // Nếu lỗi (ví dụ đã mua rồi), quay lại trang checkout và báo lỗi
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/registration/checkout?courseId="
                    + request.getCourseId() + "&packageId=" + request.getPackageId();
        }
    }

    // Các hàm Cancel, Show Pending cũ có thể giữ nguyên hoặc bỏ tùy nhu cầu
    // Nhưng với luồng tự động thì ít khi dùng đến trang "Pending" thủ công nữa.
}