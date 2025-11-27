package com.codecampus.controller.admin;

import com.codecampus.entity.Quiz;
import com.codecampus.repository.QuizRepository;
import com.codecampus.service.CourseService;
import com.codecampus.service.QuizService;
import com.codecampus.repository.TestTypeRepository;
import com.codecampus.repository.QuestionLevelRepository;
import com.codecampus.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminQuizController {

    @Autowired private QuizRepository quizRepository;
    @Autowired private QuizService quizService;
    @Autowired private CourseRepository courseRepository; // Để lấy list môn học lọc
    @Autowired private TestTypeRepository testTypeRepository;
    @Autowired private QuestionLevelRepository questionLevelRepository;

    @GetMapping("/quizzes")
    public String showQuizzes(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) Integer typeId,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        if (keyword != null && keyword.trim().isEmpty()) keyword = null;

        Page<Quiz> quizPage = quizService.getQuizzesAdmin(keyword, courseId, typeId, page, 10);

        // Map thêm thông tin phụ (Số câu hỏi, Có được sửa không)
        Map<Integer, Integer> questionCounts = new HashMap<>();
        Map<Integer, Boolean> canEditMap = new HashMap<>();

        for (Quiz q : quizPage) {
            questionCounts.put(q.getId(), quizService.getQuestionCount(q.getId()));
            canEditMap.put(q.getId(), quizService.canEditOrDelete(q.getId()));
        }

        model.addAttribute("quizPage", quizPage);
        model.addAttribute("questionCounts", questionCounts);
        model.addAttribute("canEditMap", canEditMap);

        // Dữ liệu cho bộ lọc & Modal
        model.addAttribute("courses", courseRepository.findAll());
        model.addAttribute("types", testTypeRepository.findAll());
        model.addAttribute("levels", questionLevelRepository.findAll());

        // Giữ state bộ lọc
        model.addAttribute("keyword", keyword);
        model.addAttribute("courseId", courseId);
        model.addAttribute("typeId", typeId);
        model.addAttribute("activePage", "quizzes");

        return "admin/quizzes";
    }

    // API Lấy chi tiết
    @GetMapping("/quizzes/api/{id}")
    @ResponseBody
    public ResponseEntity<?> getQuizApi(@PathVariable Integer id) {
        try {
            Quiz q = quizService.getQuizById(id);
            Map<String, Object> data = new HashMap<>();
            data.put("id", q.getId());
            data.put("name", q.getName());
            data.put("courseId", q.getCourse() != null ? q.getCourse().getId() : "");
            data.put("typeId", q.getTestType() != null ? q.getTestType().getId() : "");
            data.put("levelId", q.getExamLevel() != null ? q.getExamLevel().getId() : "");
            data.put("duration", q.getDurationMinutes());
            data.put("passRate", q.getPassRatePercentage());
            data.put("description", q.getDescription());

            // Check quyền sửa trả về cho FE biết
            data.put("canEdit", quizService.canEditOrDelete(id));

            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Xóa Quiz
    @PostMapping("/quizzes/delete/{id}")
    public String deleteQuiz(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            quizService.deleteQuiz(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa bài kiểm tra thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/quizzes";
    }

    // Lưu Quiz (Nếu bạn muốn tạo Quiz độc lập, không gắn vào Lesson ngay)
    @PostMapping("/quizzes/save")
    public String saveQuiz(@ModelAttribute Quiz quiz,
                           @RequestParam(required = false) Integer courseId,
                           @RequestParam(value = "testType.id", required = false) Integer typeId,
                           @RequestParam(value = "examLevel.id", required = false) Integer levelId,
                           RedirectAttributes redirectAttributes) {

        // Validate thô: Check nếu người dùng chưa chọn dropdown
        if (courseId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn Môn học.");
            return "redirect:/admin/quizzes";
        }
        if (typeId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn Loại bài thi.");
            return "redirect:/admin/quizzes";
        }
        if (levelId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn Cấp độ.");
            return "redirect:/admin/quizzes";
        }

        try {
            // Gọi Service (đã có full logic validate bên trong)
            quizService.saveQuiz(quiz, courseId, typeId, levelId);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu bài kiểm tra thành công!");
        } catch (Exception e) {
            // Bắt lỗi logic (Trùng tên, Đã có người thi...)
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        return "redirect:/admin/quizzes";
    }
}