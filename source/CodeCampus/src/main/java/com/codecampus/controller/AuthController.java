package com.codecampus.controller;

import com.codecampus.dto.RegistrationDto;
import com.codecampus.entity.User;
import com.codecampus.entity.VerificationToken;
import com.codecampus.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // templates/login.html
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String email,
                               @RequestParam String password,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {

        User authenticatedUser = userService.authenticate(email, password);

        if (authenticatedUser != null) {
            // Đăng nhập thành công -> Tạo session
            HttpSession session = request.getSession(true);
            session.setAttribute("loggedInUserId", authenticatedUser.getId());
            session.setAttribute("loggedInUserEmail", authenticatedUser.getEmail());
            session.setAttribute("loggedInUserFullName", authenticatedUser.getFullName());
            session.setAttribute("loggedInUserRole", authenticatedUser.getRole().getName());

            session.setMaxInactiveInterval(30 * 60); // 30 phút

            return "redirect:/dashboard";
        } else {
            // Đăng nhập thất bại
            redirectAttributes.addFlashAttribute("loginError",
                    "Email, mật khẩu không đúng hoặc tài khoản chưa được kích hoạt.");
            return "redirect:/login";
        }
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("userDto", new RegistrationDto());
        return "register"; // templates/register.html
    }

    @PostMapping("/register")
    public String processRegistration(
            @Valid @ModelAttribute("userDto") RegistrationDto dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) { // Cần request để lấy Base URL

        if (result.hasErrors()) {
            return "register"; // Trả về form nếu validation lỗi
        }

        try {
            // Lấy Base URL (vd: http://localhost:8080)
            String baseUrl = request.getRequestURL().toString().replace(request.getServletPath(), "");

            userService.registerNewUser(dto, baseUrl);

        } catch (RuntimeException e) {
            // Lỗi nghiệp vụ (email tồn tại, pass không khớp)
            model.addAttribute("registrationError", e.getMessage());
            return "register";
        }

        redirectAttributes.addFlashAttribute("registrationSuccess",
                "Đăng ký thành công! Vui lòng kiểm tra email để kích hoạt tài khoản.");
        return "redirect:/login";
    }

    @GetMapping("/verify")
    public String handleVerification(@RequestParam("token") String token,
                                     RedirectAttributes redirectAttributes) {
        try {
            userService.verifyUser(token);
            redirectAttributes.addFlashAttribute("verificationSuccess",
                    "Tài khoản của bạn đã được kích hoạt thành công. Vui lòng đăng nhập.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("verificationError", e.getMessage());
        }

        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); // Hủy session
        }
        return "redirect:/login?logout=true";
    }
    // --- CÁC ENDPOINT MỚI CHO QUÊN MẬT KHẨU ---

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password"; // Trả về templates/forgot-password.html
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email,
                                        HttpServletRequest request,
                                        RedirectAttributes redirectAttributes) {
        try {
            String baseUrl = request.getRequestURL().toString().replace(request.getServletPath(), "");
            userService.processForgotPassword(email, baseUrl);

            redirectAttributes.addFlashAttribute("forgotSuccess",
                    "Nếu email của bạn tồn tại trong hệ thống và đã được kích hoạt, " +
                            "chúng tôi đã gửi một liên kết đặt lại mật khẩu.");
        } catch (Exception e) {
            // Trường hợp chung
            redirectAttributes.addFlashAttribute("forgotError", "Đã xảy ra lỗi. Vui lòng thử lại.");
        }
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        // Xác thực token trước khi hiển thị form
        Optional<VerificationToken> tokenOpt = userService.validatePasswordResetToken(token);
        if (tokenOpt.isEmpty()) {
            model.addAttribute("error", "Token không hợp lệ hoặc đã hết hạn.");
            return "message-page"; // Tạo một trang thông báo chung
        }

        model.addAttribute("token", token);
        return "reset-password"; // Trả về templates/reset-password.html
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("password") String password,
                                       @RequestParam("confirmPassword") String confirmPassword,
                                       RedirectAttributes redirectAttributes) {
        try {
            userService.resetPassword(token, password, confirmPassword);
            redirectAttributes.addFlashAttribute("verificationSuccess",
                    "Mật khẩu của bạn đã được đặt lại thành công. Vui lòng đăng nhập.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            // Giữ người dùng ở lại trang và báo lỗi
            redirectAttributes.addFlashAttribute("resetError", e.getMessage());
            return "redirect:/reset-password?token=" + token;
        }
    }
}