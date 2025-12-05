package com.codecampus.controller;

import com.codecampus.dto.PasswordDto;
import com.codecampus.entity.User;
import com.codecampus.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null) return null;
        String email;
        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User) {
            email = ((OAuth2User) principal).getAttribute("email");
        } else {
            email = authentication.getName();
        }
        return userService.findUserByEmail(email);
    }

    private boolean isOAuth2User(Authentication authentication) {
        return authentication.getPrincipal() instanceof OAuth2User;
    }

    @GetMapping("/profile")
    public String showProfileForm(Model model, Authentication authentication) {
        User user = getCurrentUser(authentication);
        if (user == null) return "redirect:/logout";

        if (!model.containsAttribute("user")) {
            model.addAttribute("user", user);
        }
        if (!model.containsAttribute("passwordDto")) {
            model.addAttribute("passwordDto", new PasswordDto());
        }

        // Gửi cờ kiểm tra xuống View
        // Nếu là Google -> View sẽ ẨN ô nhập "Mật khẩu cũ" đi
        model.addAttribute("isGoogleAccount", isOAuth2User(authentication));

        return "profile";
    }

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
            return "redirect:/profile#general";
        }

        try {
            userService.updateProfile(currentUser, user.getFullName(), user.getMobile(), user.getGender(), user.getAddress());
            redirectAttributes.addFlashAttribute("profileSuccess", "Thông tin cá nhân đã được cập nhật thành công.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("profileError", "Lỗi cập nhật: " + e.getMessage());
        }
        return "redirect:/profile#general";
    }

    // ===============================================================
    // 3. ĐỔI MẬT KHẨU (XỬ LÝ CẢ 2 TRƯỜNG HỢP)
    // ===============================================================
    @PostMapping("/profile/change-password")
    public String changePassword(
            @Valid @ModelAttribute("passwordDto") PasswordDto passwordDto,
            BindingResult result,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        boolean isGoogleUser = isOAuth2User(authentication);

        // 1. Xử lý Validation Errors
        if (result.hasErrors()) {
            // Lọc lỗi: Nếu là Google User, ta BỎ QUA lỗi của trường "oldPassword"
            // Vì Google User không nhập oldPassword nên chắc chắn field này sẽ null/empty -> gây lỗi
            List<FieldError> errors = result.getFieldErrors();
            boolean hasRealErrors = false;

            for (FieldError error : errors) {
                // Nếu lỗi KHÔNG PHẢI do oldPassword gây ra -> Là lỗi thật
                // HOẶC nếu là User thường mà lỗi oldPassword -> Cũng là lỗi thật
                if (!error.getField().equals("oldPassword") || !isGoogleUser) {
                    hasRealErrors = true;
                    break;
                }
            }

            if (hasRealErrors) {
                redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.passwordDto", result);
                redirectAttributes.addFlashAttribute("passwordDto", passwordDto);
                redirectAttributes.addFlashAttribute("profileError", "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại.");
                return "redirect:/profile#security";
            }
        }

        try {
            String email = authentication.getName();
            // User Google lấy email kiểu khác
            if (isGoogleUser) {
                email = ((OAuth2User) authentication.getPrincipal()).getAttribute("email");
            }

            if (isGoogleUser) {
                // TRƯỜNG HỢP 1: GOOGLE USER (Tạo mật khẩu mới)
                // Gọi hàm update password trực tiếp, bỏ qua check pass cũ
                // Yêu cầu Service phải có hàm: updatePasswordWithoutOldCheck(email, newPass)
                userService.updatePasswordWithoutOldCheck(email, passwordDto.getNewPassword());
                redirectAttributes.addFlashAttribute("profileSuccess", "Đã tạo mật khẩu mới thành công! Giờ bạn có thể đăng nhập bằng mật khẩu này.");
            } else {
                // TRƯỜNG HỢP 2: USER THƯỜNG (Đổi mật khẩu)
                userService.changePassword(email, passwordDto);
                redirectAttributes.addFlashAttribute("profileSuccess", "Đổi mật khẩu thành công!");
            }

            redirectAttributes.addFlashAttribute("passwordDto", new PasswordDto());
            return "redirect:/profile#security";

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("passwordDto", passwordDto);
            redirectAttributes.addFlashAttribute("profileError", e.getMessage());
            return "redirect:/profile#security";
        }
    }

    @PostMapping("/profile/avatar-upload")
    public String uploadAvatar(
            @RequestParam("avatarFile") MultipartFile avatarFile,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        // ... (Giữ nguyên code cũ)
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/logout";
        if (avatarFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("profileError", "Vui lòng chọn file ảnh để upload.");
            return "redirect:/profile";
        }
        try {
            userService.updateAvatar(currentUser, avatarFile);
            redirectAttributes.addFlashAttribute("profileSuccess", "Ảnh đại diện đã được cập nhật.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("profileError", "Lỗi upload: " + e.getMessage());
        }
        return "redirect:/profile";
    }
}