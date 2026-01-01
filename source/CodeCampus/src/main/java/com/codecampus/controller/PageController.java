package com.codecampus.controller;

import com.codecampus.dto.ContactDTO;
import com.codecampus.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PageController {
    @Autowired
    private EmailService emailService;

    // 1. Trang Kho công cụ AI
    @GetMapping("/ai-tools")
    public String showAiToolsPage(Model model) {
        model.addAttribute("pageTitle", "Kho công cụ AI - CodeCampus");
        return "/support/ai-tools"; // Trả về file templates/ai-tools.html
    }

    // 2. Trang Giới thiệu
    @GetMapping("/about")
    public String showAboutPage(Model model) {
        model.addAttribute("pageTitle", "Về chúng tôi - CodeCampus");
        return "/support/about"; // Trả về file templates/about.html
    }

    // 3. Trang Liên hệ
    @GetMapping("/contact")
    public String showContactPage(Model model) {
        model.addAttribute("contactDTO", new ContactDTO()); // Tạo object rỗng để hứng form
        model.addAttribute("pageTitle", "Liên hệ - CodeCampus");
        return "/support/contact"; // Trả về file templates/contact.html
    }
    // Xử lý khi bấm nút Gửi
    @PostMapping("/contact")
    public String submitContact(@ModelAttribute ContactDTO contactDTO) {
        // Gọi service gửi mail
        emailService.sendContactEmail(contactDTO);

        // Chuyển hướng lại trang contact kèm thông báo thành công
        return "redirect:/contact?success";
    }

    // 4. Trang Điều khoản & Chính sách
    @GetMapping("/terms")
    public String showTermsPage() {
        return "/support/terms";
    }

    // 5. Trang Tuyển dụng
    @GetMapping("/careers")
    public String showCareersPage(Model model) {
        model.addAttribute("pageTitle", "Cơ hội việc làm - CodeCampus");
        return "/support/careers"; // Cần tạo file templates/careers.html
    }

    // 6. Trang Chính sách bảo mật
    @GetMapping("/privacy")
    public String showPrivacyPage(Model model) {
        model.addAttribute("pageTitle", "Chính sách bảo mật - CodeCampus");
        return "/support/privacy"; // Cần tạo file templates/privacy.html
    }
    // Trang Thông báo
    @GetMapping("/notifications")
    public String showNotificationsPage(Model model) {
        model.addAttribute("pageTitle", "Thông báo - CodeCampus");
        // Bạn có thể load danh sách thông báo từ DB tại đây nếu muốn
        return "notifications"; // Cần tạo file templates/notifications.html
    }
    @PostMapping("/subscribe")
    public String subscribeNewsletter(
            @RequestParam("email") String email,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        // 1. Null / blank check
        if (email == null || email.trim().isEmpty()) {
            redirectAttributes.addAttribute("error", "empty-email");
            return "redirect:" + request.getHeader("Referer");
        }

        email = email.trim();

        // 2. Length check (chuẩn RFC ~ 254)
        if (email.length() > 254) {
            redirectAttributes.addAttribute("error", "email-too-long");
            return "redirect:" + request.getHeader("Referer");
        }

        // 3. Regex check (vừa đủ, không overkill)
        String EMAIL_REGEX = "^[^\\s@]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!email.matches(EMAIL_REGEX)) {
            redirectAttributes.addAttribute("error", "invalid-email");
            return "redirect:" + request.getHeader("Referer");
        }

        // 4. Domain basic sanity check (chặn abc@localhost, abc@.)
        String domain = email.substring(email.indexOf("@") + 1);
        if (!domain.contains(".")) {
            redirectAttributes.addAttribute("error", "invalid-domain");
            return "redirect:" + request.getHeader("Referer");
        }

        // 5. Gửi mail an toàn
        try {
            emailService.sendSubscriptionEmail(email);
            redirectAttributes.addAttribute("subscribed", "true");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "send-failed");
        }

        return "redirect:" + request.getHeader("Referer");
    }

}