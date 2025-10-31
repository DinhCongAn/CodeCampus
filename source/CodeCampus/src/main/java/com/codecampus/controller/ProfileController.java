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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    // --- Lấy thông tin User hiện tại ---
    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userService.findUserByEmail(email);
    }

    // ===============================================================
    // 1. CHỨC NĂNG HIỂN THỊ PROFILE (VÀ TRUYỀN DỮ LIỆU LÊN HEADER)
    // ===============================================================

    @GetMapping("/profile")
    public String showProfileForm(Model model, Authentication authentication) {
        User user = getCurrentUser(authentication);
        if (user == null) {
            return "redirect:/logout";
        }

//        // --- LOGIC TRUYỀN DỮ LIỆU USER ĐẾN TEMPLATE VÀ HEADER ---
//        // Các biến này được sử dụng trong header.html
//        model.addAttribute("fullName", user.getFullName());
//        model.addAttribute("avatarUrl", user.getAvatarUrl());
//        model.addAttribute("roleName", user.getRole().getName()); // Dùng cho menu dropdown kiểm tra quyền
//        model.addAttribute("email", user.getEmail()); // Dùng cho hiển thị email trong dropdown
//        // ---------------------------------------------------------

        // Gửi đối tượng User hiện tại để điền vào form
        model.addAttribute("user", user);

        // Gửi đối tượng DTO rỗng để điền vào form đổi mật khẩu
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
            @ModelAttribute("user") User user,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/logout";

        try {
            userService.updateProfile(
                    currentUser,
                    user.getFullName(),
                    user.getMobile(),
                    user.getGender(),
                    user.getAddress()
            );
            redirectAttributes.addFlashAttribute("profileSuccess", "Thông tin cá nhân đã được cập nhật thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("profileError", "Lỗi: " + e.getMessage());
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
            // THÊM THAM SỐ NÀY
            HttpServletRequest request) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.passwordDto", result);
            redirectAttributes.addFlashAttribute("passwordDto", passwordDto);
            return "redirect:/profile";
        }

        try {
            String email = authentication.getName();
            userService.changePassword(email, passwordDto);

            // =========================================================
            // 1. BUỘC ĐĂNG XUẤT THỦ CÔNG (ĐẢM BẢO XÓA SESSION)
            // =========================================================
            // Xóa bối cảnh bảo mật (Security Context)
            SecurityContextHolder.getContext().setAuthentication(null);

            // Hủy session HTTP
            if (request.getSession(false) != null) {
                request.getSession(false).invalidate();
            }
            // =========================================================

            redirectAttributes.addFlashAttribute("verificationSuccess",
                    "Mật khẩu đã được đổi thành công. Vui lòng đăng nhập lại.");

            // Chuyển hướng TRỰC TIẾP đến trang đăng nhập (vì session đã bị hủy)
            return "redirect:/home";

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("passwordError", e.getMessage());
            return "redirect:/profile";
        }
    }

    // ===============================================================
    // 4. ĐỔI ẢNH ĐẠI DIỆN (AVATAR)
    // ===============================================================

    @PostMapping("/profile/avatar-upload")
    public String uploadAvatar(
            @RequestParam("avatarFile") MultipartFile avatarFile,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/logout";

        try {
            userService.updateAvatar(currentUser, avatarFile);
            redirectAttributes.addFlashAttribute("avatarSuccess", "Ảnh đại diện đã được cập nhật.");

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("avatarError", e.getMessage());
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("avatarError", "Lỗi lưu file: " + e.getMessage());
        }

        return "redirect:/profile";
    }
}