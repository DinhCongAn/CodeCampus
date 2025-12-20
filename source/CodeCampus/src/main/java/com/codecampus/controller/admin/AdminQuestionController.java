package com.codecampus.controller.admin;

import com.codecampus.dto.GeneratedQuestionDTO;
import com.codecampus.dto.QuestionSaveRequest; // <<-- Bổ sung
import com.codecampus.dto.SaveResultDetail;   // <<-- Bổ sung
import com.codecampus.dto.SaveResultDetail.FailEntry; // <<-- Bổ sung
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
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "levelId", required = false) Integer levelId,
            Model model
    ) {
        Page<Question> questionsPage = questionService.getQuestionsByFilters(keyword, courseId, quizId, levelId, status, page, 10);
        model.addAttribute("qPage", questionsPage);
        model.addAttribute("courses", questionService.getAllCourses());
        model.addAttribute("levels", questionService.getAllLevels());

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
            @ModelAttribute Question question,
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
            @RequestParam(value = "courseId", required = true) Integer courseId,
            @RequestParam(value = "quizId", required = false) Integer quizId,
            @RequestParam(value = "lessonId", required = false) Integer lessonId
    ) {
        try {
            if (file.isEmpty()) throw new IllegalArgumentException("File trống.");

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

    // API lấy Lesson theo Course
    @GetMapping("/api/lessons-by-course")
    @ResponseBody
    public List<Map<String, Object>> getLessonsApi(@RequestParam Integer courseId) {
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
                    map.put("lessonId", q.getLesson() != null ? q.getLesson().getId() : null);

                    if (q.getQuizzes() != null && !q.getQuizzes().isEmpty()) {
                        map.put("quizId", q.getQuizzes().get(0).getId());
                    }

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

    @PostMapping("/toggle/{id}")
    public String toggleStatus(@PathVariable Integer id,
                               RedirectAttributes redirectAttributes,
                               HttpServletRequest request) {
        try {
            questionService.toggleStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái câu hỏi.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/admin/questions");
    }

    // API 1: Trigger AI sinh câu hỏi (Preview)
    @PostMapping("/api/ai-generate")
    @ResponseBody
    public ResponseEntity<?> generateQuestions(
            @RequestParam Integer courseId,
            @RequestParam(required = false) Integer quizId,
            @RequestParam(required = false) Integer lessonId,
            @RequestParam(required = false) Integer levelId,
            @RequestParam(defaultValue = "5") int count,
            @RequestParam(required = false) String description
    ) {
        try {
            List<GeneratedQuestionDTO> previewList = questionService.generateQuestionsWithAi(courseId, quizId, lessonId, levelId, description, count);
            return ResponseEntity.ok(previewList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi sinh AI: " + e.getMessage());
        }
    }

    // API 2: Lưu danh sách câu hỏi đã duyệt - ĐÃ SỬA ĐỂ DÙNG SAVE RESULT DETAIL
    @PostMapping("/api/ai-save-batch")
    @ResponseBody
    public ResponseEntity<SaveResultDetail> saveBatchQuestions(@RequestBody List<QuestionSaveRequest> questions) {
        try {
            // Gọi hàm mới trong Service, nó sẽ thực hiện Validation và trả về báo cáo
            SaveResultDetail result = questionService.saveAiGeneratedQuestions(questions);

            // Trả về báo cáo chi tiết để Frontend hiển thị Modal
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            // Xử lý lỗi hệ thống bất ngờ (Lỗi 500)
            SaveResultDetail errorResult = SaveResultDetail.builder()
                    .totalRecords(questions != null ? questions.size() : 0)
                    .failCount(questions != null ? questions.size() : 0)
                    .failDetails(List.of(
                            FailEntry.builder()
                                    .sourceData("Lỗi hệ thống")
                                    .errorMessage("Lỗi server không xác định: " + e.getMessage())
                                    .build()
                    ))
                    .build();
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }
}