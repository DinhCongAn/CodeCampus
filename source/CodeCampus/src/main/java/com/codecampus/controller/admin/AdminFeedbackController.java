package com.codecampus.controller.admin;

import com.codecampus.entity.Feedback;
import com.codecampus.entity.FeedbackAttachment;
import com.codecampus.service.CourseService;
import com.codecampus.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/feedbacks")
public class AdminFeedbackController {

    @Autowired private FeedbackService feedbackService;
    @Autowired private CourseService courseService;

    @GetMapping
    public String listFeedbacks(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(required = false) Integer rating,   // [MỚI]
                                @RequestParam(required = false) Integer courseId, // [MỚI]
                                Model model) {

        Page<Feedback> feedbackPage = feedbackService.getFeedbacksForAdmin(keyword, rating, courseId, page, size);

        model.addAttribute("feedbackPage", feedbackPage);
        model.addAttribute("currentPage", page);

        // Truyền lại giá trị đã chọn để giữ trạng thái trên form
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedRating", rating);
        model.addAttribute("selectedCourseId", courseId);

        // Lấy danh sách tất cả khóa học để đổ vào dropdown filter
        model.addAttribute("courses", courseService.getAllCourses());

        return "admin/feedback-list";
    }

    // API lấy chi tiết Feedback cho Modal (Ajax)
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> getFeedbackDetail(@PathVariable Integer id) {
        try {
            Feedback fb = feedbackService.findById(id);
            Map<String, Object> response = new HashMap<>();

            response.put("id", fb.getId());
            response.put("userFullName", fb.getUser().getFullName());
            response.put("userEmail", fb.getUser().getEmail());
            // Xử lý avatar: nếu null thì FE sẽ tự xử lý hoặc trả về null
            response.put("userAvatar", fb.getUser().getAvatarUrl() != null ? fb.getUser().getAvatarUrl() : null);
            response.put("courseName", fb.getCourse().getName());
            response.put("rating", fb.getRating());
            response.put("comment", fb.getComment());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            response.put("createdAt", fb.getCreatedAt().format(formatter));

            // Lấy danh sách ảnh
            List<String> images = fb.getAttachments().stream()
                    .map(FeedbackAttachment::getFileUrl)
                    .collect(Collectors.toList());
            response.put("attachments", images);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
}