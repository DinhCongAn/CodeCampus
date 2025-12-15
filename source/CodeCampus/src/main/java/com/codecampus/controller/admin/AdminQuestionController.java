package com.codecampus.controller.admin;

import com.codecampus.entity.Lesson;
import com.codecampus.entity.Question;
import com.codecampus.repository.LessonRepository;
import com.codecampus.service.QuestionService;
import jakarta.servlet.http.HttpServletRequest;
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
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/questions")
public class AdminQuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private LessonRepository lessonRepository;

    @GetMapping("")
    public String showQuestions(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "courseId", required = false) Integer courseId,
            @RequestParam(value = "quizId", required = false) Integer quizId,

            // 1. Nhận tham số status từ form HTML
            @RequestParam(value = "status", required = false) String status,

            @RequestParam(value = "levelId", required = false) Integer levelId,
            Model model
    ) {
        // 2. Truyền status vào Service (lúc này service sẽ quyết định null hay có giá trị)
        Page<Question> questionsPage = questionService.getQuestionsByFilters(keyword, courseId, quizId, levelId, status, page, 10);
        model.addAttribute("qPage", questionsPage);
        model.addAttribute("courses", questionService.getAllCourses());
        model.addAttribute("levels", questionService.getAllLevels());

        // 3. Trả lại các giá trị filter để giao diện hiển thị lại (giữ trạng thái dropdown)
        model.addAttribute("keyword", keyword);
        model.addAttribute("courseId", courseId);
        model.addAttribute("quizId", quizId);
        model.addAttribute("levelId", levelId);
        model.addAttribute("status", status);

        if (courseId != null) {
            model.addAttribute("quizzes", questionService.getQuizzesByCourseId(courseId));
        }

        return "admin/questions";
    }

    // --- 2. LƯU (THÊM / SỬA) ---
    @PostMapping("/save")
    public String saveQuestion(
            @ModelAttribute Question question, // Bind các field: content, course.id, lesson.id, level.id
            @RequestParam(value = "id", required = false) Integer id,
            @RequestParam(value = "quizId", required = false) Integer quizId,
            @RequestParam(value = "correctIndex", required = false) Integer correctIndex,
            RedirectAttributes redirectAttributes
    ) {
        try {
            questionService.saveOrUpdateQuestion(id, question, correctIndex, quizId);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/questions";
    }

    // --- 3. IMPORT EXCEL ---

    @PostMapping("/import")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> importQuestions(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "courseId", required = true) Integer courseId, // Bắt buộc
            @RequestParam(value = "quizId", required = false) Integer quizId,
            @RequestParam(value = "lessonId", required = false) Integer lessonId
    ) {
        try {
            if (file.isEmpty()) throw new IllegalArgumentException("File trống.");

            // Gọi Service với đầy đủ tham số
            Map<String, Object> result = questionService.importQuestionsFromExcel(file, courseId, quizId, lessonId);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Lỗi Server: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // --- 4. TẢI TEMPLATE ---
    @GetMapping("/download-template")
    public ResponseEntity<InputStreamResource> downloadTemplate() throws IOException {
        ByteArrayInputStream in = questionService.generateImportTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=Question_Template.xlsx");
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    // --- 5. APIs CHO AJAX (JAVASCRIPT) ---

    // API lấy Quiz theo Course
    @GetMapping("/api/quizzes-by-course")
    @ResponseBody
    public List<Map<String, Object>> getQuizzesApi(@RequestParam Integer courseId) {
        return questionService.getQuizzesByCourseId(courseId).stream().map(q -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", q.getId());
            map.put("name", q.getName());
            return map;
        }).collect(Collectors.toList());
    }

    // API lấy Lesson theo Course (FIX: Thêm mới)
    @GetMapping("/api/lessons-by-course")
    @ResponseBody
    public List<Map<String, Object>> getLessonsApi(@RequestParam Integer courseId) {
        // Gọi repo lấy lessons
        List<Lesson> lessons = lessonRepository.findByCourseId(courseId);
        return lessons.stream().map(l -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", l.getId());
            map.put("name", l.getName());
            return map;
        }).collect(Collectors.toList());
    }

    // API lấy chi tiết câu hỏi (Edit Modal)
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> getQuestionDetailApi(@PathVariable Integer id) {
        return questionService.getQuestionById(id)
                .map(q -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", q.getId());
                    map.put("content", q.getContent());
                    map.put("explanation", q.getExplanation());
                    map.put("courseId", q.getCourse() != null ? q.getCourse().getId() : null);
                    map.put("levelId", q.getQuestionLevel() != null ? q.getQuestionLevel().getId() : null);
                    // FIX: Map lesson ID
                    map.put("lessonId", q.getLesson() != null ? q.getLesson().getId() : null);

                    // Lấy Quiz đầu tiên (Logic đơn giản hóa cho UI)
                    if (q.getQuizzes() != null && !q.getQuizzes().isEmpty()) {
                        map.put("quizId", q.getQuizzes().get(0).getId());
                    }

                    // Map Answers
                    List<Map<String, Object>> answers = q.getAnswerOptions().stream().map(a -> {
                        Map<String, Object> am = new HashMap<>();
                        am.put("content", a.getContent());
                        am.put("correct", a.getIsCorrect());
                        return am;
                    }).collect(Collectors.toList());
                    map.put("answers", answers);

                    return ResponseEntity.ok(map);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/toggle/{id}") // Dùng POST cho chuẩn bảo mật
    public String toggleStatus(@PathVariable Integer id,
                               RedirectAttributes redirectAttributes,
                               HttpServletRequest request) { // Thêm Request để lấy trang hiện tại
        try {
            questionService.toggleStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái câu hỏi.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        // Mẹo: Redirect về đúng trang người dùng đang đứng (để đỡ bị nhảy về trang chủ)
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/admin/questions");
    }
}