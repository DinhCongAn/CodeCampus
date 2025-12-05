package com.codecampus.controller.admin;

import com.codecampus.dto.QuestionForm;
import com.codecampus.entity.Quiz;
import com.codecampus.entity.Question;
import com.codecampus.repository.CourseRepository;
import com.codecampus.repository.QuestionLevelRepository;
import com.codecampus.repository.QuizRepository;
import com.codecampus.service.QuestionService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/questions")
public class AdminQuestionController {

    @Autowired private QuestionService questionService;
    @Autowired private CourseRepository courseRepository;
    @Autowired private QuestionLevelRepository levelRepository;
    @Autowired private QuizRepository quizRepository;

    // =========================================================================
    // 1. HIỂN THỊ DANH SÁCH (PAGE + FILTER)
    // =========================================================================
    @GetMapping
    public String showQuestions(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) Integer quizId,
            @RequestParam(required = false) Integer levelId,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        // 1. Lấy dữ liệu phân trang từ Service
        Page<Question> qPage = questionService.getQuestionsAdmin(keyword, courseId, quizId, levelId, page, 10);

        // 2. Đẩy dữ liệu vào Model
        model.addAttribute("qPage", qPage);
        model.addAttribute("courses", courseRepository.findAll());
        model.addAttribute("levels", levelRepository.findAll());

        // Để dropdown Quiz không bị trống khi load lại trang (nếu đang lọc)
        if (courseId != null) {
            model.addAttribute("quizzes", quizRepository.findByCourseId(Long.valueOf(courseId))); // Cần method này trong QuizRepository
        } else {
            model.addAttribute("quizzes", quizRepository.findAll());
        }

        // 3. Giữ lại giá trị bộ lọc để hiển thị trên UI
        model.addAttribute("keyword", keyword);
        model.addAttribute("courseId", courseId);
        model.addAttribute("quizId", quizId);
        model.addAttribute("levelId", levelId);
        model.addAttribute("activePage", "questions"); // Để highlight menu sidebar

        return "admin/questions";
    }

    // =========================================================================
    // 2. API CHO JAVASCRIPT (AJAX)
    // =========================================================================

    /**
     * API: Lấy chi tiết câu hỏi để hiển thị lên Modal Sửa
     */
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<QuestionForm> getQuestionDetail(@PathVariable Integer id) {
        QuestionForm form = questionService.getQuestionDetail(id);
        return ResponseEntity.ok(form);
    }

    /**
     * API: Lấy danh sách Quiz theo Môn học (Dùng cho Cascading Dropdown)
     */
    @GetMapping("/api/quizzes-by-course")
    @ResponseBody
    public List<QuizDTO> getQuizzesByCourse(@RequestParam Integer courseId) {
        // Lưu ý: QuizRepository phải có method List<Quiz> findByCourseId(Integer id);
        List<Quiz> quizzes = quizRepository.findByCourseId(Long.valueOf(courseId));

        // Map sang DTO đơn giản để tránh lỗi vòng lặp JSON vô tận
        return quizzes.stream()
                .map(q -> new QuizDTO(q.getId(), q.getName()))
                .collect(Collectors.toList());
    }

    // DTO tĩnh dùng nội bộ cho API trên
    @Data
    @AllArgsConstructor
    public static class QuizDTO {
        private Integer id;
        private String name;
    }

    // =========================================================================
    // 3. CÁC HÀNH ĐỘNG (ACTIONS)
    // =========================================================================

    // A. LƯU THỦ CÔNG (Thêm mới / Cập nhật)
    @PostMapping("/save")
    public String saveManual(@ModelAttribute QuestionForm form, RedirectAttributes ra) {
        try {
            questionService.saveQuestionManual(form);
            ra.addFlashAttribute("successMessage", "Lưu câu hỏi thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("errorMessage", "Lỗi lưu dữ liệu: " + e.getMessage());
        }
        return "redirect:/admin/questions";
    }

    // B. AI GENERATE
    @PostMapping("/generate-ai")
    public String generateAi(@RequestParam Integer courseId,
                             @RequestParam(required = false) Integer quizId, // Optional
                             @RequestParam Integer levelId,
                             @RequestParam String topic,
                             @RequestParam int numQuestions,
                             RedirectAttributes ra) {
        try {
            questionService.generateQuestionsAi(courseId, quizId, levelId, topic, numQuestions);
            ra.addFlashAttribute("successMessage", "AI đã tạo " + numQuestions + " câu hỏi thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("errorMessage", "Lỗi AI: " + e.getMessage());
        }
        return "redirect:/admin/questions";
    }

    // C. IMPORT EXCEL (QuizId là bắt buộc)
    @PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file,
                              @RequestParam Integer quizId, // <-- Bắt buộc Quiz
                              RedirectAttributes ra) {
        try {
            // Không cần courseId nữa vì quizId đã xác định được course
            questionService.importFromExcel(file, quizId);
            ra.addFlashAttribute("successMessage", "Import file Excel thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("errorMessage", "Lỗi Import: " + e.getMessage());
        }
        return "redirect:/admin/questions";
    }

    // D. TẢI FILE MẪU
    @GetMapping("/download-template")
    public ResponseEntity<InputStreamResource> downloadTemplate() {
        ByteArrayInputStream in = questionService.generateExcelTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=mau_nhap_cau_hoi.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}