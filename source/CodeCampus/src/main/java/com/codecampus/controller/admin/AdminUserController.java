package com.codecampus.controller.admin;

import com.codecampus.entity.User;
import com.codecampus.repository.UserRoleRepository; // Sửa thành UserRoleRepository
import com.codecampus.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminUserController {

    @Autowired private UserService userService;
    @Autowired private UserRoleRepository userRoleRepository; // Repo của bảng user_roles

    // 1. Hiển thị danh sách
    @GetMapping("/users")
    public String showUsers(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Integer roleId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "7") int size,
            Model model) {

        if (keyword != null && keyword.trim().isEmpty()) keyword = null;
        if (status != null && status.trim().isEmpty()) status = null;

        Page<User> userPage = userService.getUsersForAdmin(keyword, roleId, status, page, size);

        model.addAttribute("userPage", userPage);
        model.addAttribute("roles", userRoleRepository.findAll()); // Load dropdown Role

        model.addAttribute("keyword", keyword);
        model.addAttribute("roleId", roleId);
        model.addAttribute("status", status);
        model.addAttribute("activePage", "users");

        return "admin/users";
    }

    // 2. API Lấy chi tiết (Map đúng với Entity mới)
    @GetMapping("/users/api/{id}")
    @ResponseBody
    public ResponseEntity<?> getUserApi(@PathVariable Integer id) {
        try {
            User user = userService.getUserById(id);
            Map<String, Object> data = new HashMap<>();
            data.put("id", user.getId());
            data.put("fullName", user.getFullName());
            data.put("email", user.getEmail());
            data.put("mobile", user.getMobile());
            data.put("address", user.getAddress());

            // Gender giờ là String
            data.put("gender", user.getGender());

            // Map avatarUrl (Entity) -> avatarUrl (JSON)
            data.put("avatarUrl", user.getAvatarUrl());

            data.put("status", user.getStatus());

            if (user.getRole() != null) {
                data.put("roleId", user.getRole().getId());
            }

            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 3. Lưu User
    @PostMapping("/users/save")
    public String saveUser(@Valid @ModelAttribute User user,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {

        // Nếu là Thêm mới (ID null) thì bắt buộc validate kỹ
        if (user.getId() == null && bindingResult.hasErrors()) {
            String msg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi nhập liệu: " + msg);
            return "redirect:/admin/users";
        }

        try {
            userService.saveUserAdmin(user);
            redirectAttributes.addFlashAttribute("successMessage",
                    user.getId() == null ? "Thêm mới thành công! Mật khẩu đã gửi qua email." : "Cập nhật thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // 4. Khóa user
    @PostMapping("/users/toggle/{id}")
    public String toggleStatus(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            userService.toggleUserStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
}