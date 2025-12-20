package com.codecampus.controller;

import com.codecampus.entity.User;
import com.codecampus.service.FeedbackService;
import com.codecampus.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired private FeedbackService feedbackService;
    @Autowired private UserService userService;

    /**
     * Xử lý gửi đánh giá (Mới hoặc Cập nhật) - Có hỗ trợ upload file
     */
    @PostMapping("/submit")
    public String submitFeedback(@RequestParam("courseId") Integer courseId,
                                 @RequestParam("rating") Integer rating,
                                 @RequestParam("comment") String comment,
                                 @RequestParam(value = "file", required = false) MultipartFile file, // <--- BỔ SUNG FILE
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            User currentUser = userService.findUserByEmail(principal.getName());

            // Gọi service mới (đã bao gồm xử lý file)
            feedbackService.submitFeedback(currentUser.getId(), courseId, rating, comment, file);

            redirectAttributes.addFlashAttribute("successMessage", "Cảm ơn bạn đã gửi đánh giá!");
        } catch (Exception e) {
            // Log lỗi ra console để debug nếu cần
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        return "redirect:/courses/" + courseId;
    }

    /**
     * BỔ SUNG: Xóa đánh giá
     */
    @PostMapping("/delete")
    public String deleteFeedback(@RequestParam("feedbackId") Integer feedbackId,
                                 @RequestParam("courseId") Integer courseId, // Cần courseId để redirect quay lại đúng trang
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            User currentUser = userService.findUserByEmail(principal.getName());

            // Gọi service xóa
            feedbackService.deleteFeedback(feedbackId, currentUser.getId());

            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa đánh giá thành công.");
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền xóa đánh giá này.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa: " + e.getMessage());
        }

        return "redirect:/courses/" + courseId;
    }
}