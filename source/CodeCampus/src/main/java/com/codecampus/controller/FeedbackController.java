package com.codecampus.controller;

import com.codecampus.service.FeedbackService;
import com.codecampus.service.UserService;
import com.codecampus.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired private FeedbackService feedbackService;
    @Autowired private UserService userService;

    @PostMapping("/submit")
    public String submitFeedback(@RequestParam("courseId") Integer courseId,
                                 @RequestParam("rating") Integer rating,
                                 @RequestParam("comment") String comment,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            User currentUser = userService.findUserByEmail(principal.getName());
            feedbackService.submitFeedback(currentUser.getId(), courseId, rating, comment);
            redirectAttributes.addFlashAttribute("successMessage", "Cảm ơn bạn đã đánh giá!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/courses/" + courseId;
    }
}