package com.codecampus.controller;

import com.codecampus.dto.ContactDTO;
import com.codecampus.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    // Xử lý khi bấm nút Subscribe
    @PostMapping("/subscribe")
    public String subscribeNewsletter(@RequestParam("email") String email) {
        // 1. Gửi email thông báo cho bạn
        emailService.sendSubscriptionEmail(email);
        // 2. Quay lại trang chủ và hiện thông báo thành công
        return "redirect:/home?subscribed";
    }
}