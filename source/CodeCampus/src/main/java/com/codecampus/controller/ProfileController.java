package com.codecampus.controller;

import com.codecampus.dto.PasswordDto;
import com.codecampus.entity.User;
import com.codecampus.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    // --- Lấy User hiện tại từ SecurityContext ---
    private User getCurrentUser(Authentication authentication) {
        if (authentication == null) return null;
        String email = authentication.getName();
        return userService.findUserByEmail(email);
    }

    // ===============================================================
    // 1. HIỂN THỊ PROFILE
    // ===============================================================
    @GetMapping("/profile")
    public String showProfileForm(Model model, Authentication authentication) {
        User user = getCurrentUser(authentication);
        if (user == null) return "redirect:/logout";

        // Gửi User hiện tại tới form
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", user);
        }

        // DTO cho đổi mật khẩu
        if (!model.containsAttribute("passwordDto")) {
            model.addAttribute("passwordDto", new PasswordDto());
        }

        return "profile";
    }

    // ===============================================================
    // 2. CẬP NHẬT THÔNG TIN CÁ NHÂN
    // ===============================================================
    @PostMapping("/profile/update")
    public String updateProfile(
            @ModelAttribute("user") @Valid User user,
            BindingResult result,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/logout";

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.user", result);
            redirectAttributes.addFlashAttribute("user", user);
            redirectAttributes.addFlashAttribute("profileError", "Thông tin nhập chưa hợp lệ, vui lòng kiểm tra lại.");
            return "redirect:/profile";
        }

        try {
            userService.updateProfile(
                    currentUser,
                    user.getFullName(),
                    user.getMobile(),
                    user.getGender(),
                    user.getAddress()
            );
            redirectAttributes.addFlashAttribute("profileSuccess", "Thông tin cá nhân đã được cập nhật thành công.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("profileError", "Lỗi cập nhật: " + e.getMessage());
        }

        return "redirect:/profile";
    }

    // ===============================================================
    // 3. ĐỔI MẬT KHẨU
    // ===============================================================
    @PostMapping("/profile/change-password")
    public String changePassword(
            @Valid @ModelAttribute("passwordDto") PasswordDto passwordDto,
            BindingResult result,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.passwordDto", result);
            redirectAttributes.addFlashAttribute("passwordDto", passwordDto);
            redirectAttributes.addFlashAttribute("passwordError", "Thông tin nhập chưa hợp lệ, vui lòng kiểm tra lại.");
            return "redirect:/profile";
        }

        try {
            String email = authentication.getName();
            userService.changePassword(email, passwordDto);

            // Buộc đăng xuất để session cũ hết hiệu lực
            SecurityContextHolder.clearContext();
            if (request.getSession(false) != null) {
                request.getSession(false).invalidate();
            }

            redirectAttributes.addFlashAttribute("passwordSuccess", "Mật khẩu đã được đổi thành công. Vui lòng đăng nhập lại.");
            return "redirect:/login"; // redirect về login

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("passwordError", e.getMessage());
            return "redirect:/profile";
        }
    }

    // ===============================================================
    // 4. ĐỔI ẢNH ĐẠI DIỆN
    // ===============================================================
    @PostMapping("/profile/avatar-upload")
    public String uploadAvatar(
            @RequestParam("avatarFile") MultipartFile avatarFile,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/logout";

        if (avatarFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("avatarError", "Vui lòng chọn file ảnh để upload.");
            return "redirect:/profile";
        }

        try {
            userService.updateAvatar(currentUser, avatarFile);
            redirectAttributes.addFlashAttribute("avatarSuccess", "Ảnh đại diện đã được cập nhật.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("avatarError", "Lỗi upload: " + e.getMessage());
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("avatarError", "Lỗi lưu file: " + e.getMessage());
        }

        return "redirect:/profile";
    }
}
