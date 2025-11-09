
package com.codecampus.controller;

import com.codecampus.dto.RegistrationRequest;
import com.codecampus.entity.Registration;
import com.codecampus.service.QrCodeService;
import com.codecampus.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegistrationController {

    @Autowired private RegistrationService registrationService;
    @Autowired private QrCodeService qrCodeService;

    // Lấy thông tin bank từ properties để hiển thị
    @Value("${payment.vietqr.account-name}") private String bankAccountName;
    @Value("${payment.vietqr.account-no}") private String bankAccountNo;
    // (Bạn có thể thêm bank-name vào properties và @Value nó ở đây)

    /**
     * BƯỚC 1: Xử lý POST form đăng ký từ Modal
     */
    @PostMapping("/register-process")
    public String handleRegistration(
            @ModelAttribute RegistrationRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            String username = (userDetails != null) ? userDetails.getUsername() : null;
            // Tạo đơn hàng PENDING
            Registration newReg = registrationService.createPendingRegistration(request, username);

            // Chuyển hướng đến trang thanh toán
            return "redirect:/registration/confirm/" + newReg.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/courses/" + request.getCourseId();
        }
    }

    /**
     * BƯỚC 2: Trang xác nhận và hiển thị QR Code
     */
    @GetMapping("/registration/confirm/{id}")
    public String showConfirmationPage(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Registration reg = registrationService.getRegistrationById(id);

            // Nếu đã thanh toán rồi thì redirect luôn
            if (!"PENDING".equals(reg.getStatus())) {
                redirectAttributes.addFlashAttribute("successMessage", "Khóa học này đã được đăng ký.");
                return "redirect:/my-courses";
            }

            String qrCodeBase64 = qrCodeService.generateVietQrBase64(reg);

            model.addAttribute("registration", reg);
            model.addAttribute("qrCodeBase64", qrCodeBase64);
            model.addAttribute("bankAccountName", bankAccountName);
            model.addAttribute("bankAccountNo", bankAccountNo);
            model.addAttribute("bankName", "Ngân hàng MBBank"); // Cứng, hoặc thêm vào properties

            return "confirm-payment"; // Trả về templates/confirm-payment.html

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/";
        }
    }
}