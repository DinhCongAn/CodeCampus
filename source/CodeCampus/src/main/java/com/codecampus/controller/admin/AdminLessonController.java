package com.codecampus.controller.admin;

import com.codecampus.entity.Lesson;
import com.codecampus.repository.*;
import com.codecampus.service.CourseService;
import com.codecampus.service.LessonService;
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
public class AdminLessonController {

    @Autowired private LessonService lessonService;
    @Autowired private CourseService courseService;
    @Autowired private LessonRepository lessonRepository; // [MỚI] Inject để tìm Max Order
    @Autowired private LessonTypeRepository lessonTypeRepository;
    @Autowired private PricePackageRepository pricePackageRepository;
    @Autowired private TestTypeRepository testTypeRepository;
    @Autowired private QuestionLevelRepository questionLevelRepository;
    @Autowired private QuizRepository quizRepository;
    // 1. HIỂN THỊ DANH SÁCH
    @GetMapping("/lessons")
    public String showLessons(
            @RequestParam("courseId") Long courseId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "") String keyword, // [MỚI]
            @RequestParam(required = false) Integer typeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "7") int size,

            Model model) {

        if (status != null && status.trim().isEmpty()) status = null;

        Page<Lesson> lessonPage = lessonService.getLessonsByCourse(courseId, keyword, typeId, status, page, size);
        // [LOGIC MỚI] TÍNH SỐ THỨ TỰ TIẾP THEO
        Integer maxOrder = lessonRepository.findMaxOrderNumber(courseId);
        model.addAttribute("nextOrder", maxOrder + 1);

        model.addAttribute("lessonPage", lessonPage);
        model.addAttribute("course", courseService.getCourseById(courseId));
        model.addAttribute("types", lessonTypeRepository.findAll());
        model.addAttribute("packages", pricePackageRepository.findByCourseId(Math.toIntExact(courseId)));
        model.addAttribute("testTypes", testTypeRepository.findAll());
        model.addAttribute("quizLevels", questionLevelRepository.findAll());// Ép kiểu .intValue() nếu courseId là Integer trong Repo

        model.addAttribute("existingQuizzes", quizRepository.findByCourseId(courseId));
        // Giữ lại giá trị bộ lọc để hiển thị trên View
        model.addAttribute("courseId", courseId);
        model.addAttribute("keyword", keyword);
        model.addAttribute("typeId", typeId);
        model.addAttribute("status", status);

        return "admin/lessons";
    }

    // 2. API LẤY CHI TIẾT (BAO GỒM CẢ LAB)
    @GetMapping("/lessons/api/{id}")
    @ResponseBody
    public ResponseEntity<?> getLessonApi(@PathVariable Long id) {
        try {
            Lesson lesson = lessonService.getLessonById(id);

            Map<String, Object> data = new HashMap<>();
            data.put("id", lesson.getId());
            data.put("name", lesson.getName());
            data.put("orderNumber", lesson.getOrderNumber());
            data.put("videoUrl", lesson.getVideoUrl());
            data.put("htmlContent", lesson.getHtmlContent());
            data.put("status", lesson.getStatus());

            if (lesson.getLessonType() != null) {
                data.put("typeId", lesson.getLessonType().getId());
            }
            if (lesson.getPricePackage() != null) {
                data.put("packageId", lesson.getPricePackage().getId());
            }

            // [MỚI] Map dữ liệu Lab nếu có
            if (lesson.getLab() != null) {
                Map<String, Object> labData = new HashMap<>();
                labData.put("id", lesson.getLab().getId());
                labData.put("name", lesson.getLab().getName());
                labData.put("labType", lesson.getLab().getLabType());
                labData.put("description", lesson.getLab().getDescription());
                labData.put("criteria", lesson.getLab().getEvaluationCriteria());
                data.put("lab", labData);
            }

            // [MỚI] Map Quiz
            if (lesson.getQuiz() != null) {
                Map<String, Object> quizData = new HashMap<>();
                quizData.put("id", lesson.getQuiz().getId());
                quizData.put("name", lesson.getQuiz().getName());
                quizData.put("duration", lesson.getQuiz().getDurationMinutes());
                quizData.put("passRate", lesson.getQuiz().getPassRatePercentage());
                quizData.put("description", lesson.getQuiz().getDescription());

                // Trả về ID để JS select đúng dropdown
                if (lesson.getQuiz().getTestType() != null)
                    quizData.put("testTypeId", lesson.getQuiz().getTestType().getId());

                if (lesson.getQuiz().getExamLevel() != null)
                    quizData.put("levelId", lesson.getQuiz().getExamLevel().getId());

                data.put("quiz", quizData);
            }

            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 3. LƯU BÀI HỌC (FULL LOGIC: FIX TRANSIENT + LAB + QUIZ)
    @PostMapping("/lessons/save")
    public String saveLesson(@Valid @ModelAttribute Lesson lesson,
                             BindingResult bindingResult,
                             @RequestParam("courseId") Long courseId,

                             // [1] FIX LỖI GÓI GIÁ (Transient Object)
                             // Hứng riêng ID để kiểm tra null
                             @RequestParam(value = "pricePackage.id", required = false) Integer packageId,

                             // [2] XỬ LÝ LAB THỦ CÔNG (Tránh lỗi Binding null)
                             @RequestParam(value = "lab.id", required = false) Integer labId,
                             @RequestParam(value = "lab.name", required = false) String labName,
                             @RequestParam(value = "lab.labType", required = false) String labType,
                             @RequestParam(value = "lab.description", required = false) String labDesc,
                             @RequestParam(value = "lab.evaluationCriteria", required = false) String labCriteria,

                             // [3] XỬ LÝ QUIZ THỦ CÔNG
                             @RequestParam(value = "quiz.id", required = false) Integer quizId,
                             @RequestParam(value = "quiz.name", required = false) String quizName,
                             @RequestParam(value = "quiz.durationMinutes", required = false) Integer quizDuration,
                             @RequestParam(value = "quiz.passRatePercentage", required = false) java.math.BigDecimal quizPassRate,
                             @RequestParam(value = "quiz.description", required = false) String quizDesc,
                             @RequestParam(value = "quiz.testType.id", required = false) Integer quizTypeId,
                             @RequestParam(value = "quiz.examLevel.id", required = false) Integer quizLevelId,
                             @RequestParam(value = "quizIdSelect", required = false) Integer quizIdSelect,

                             RedirectAttributes redirectAttributes) {

        // 1. Validate cơ bản (Bỏ qua lỗi của đối tượng con nếu không nhập)
        if (bindingResult.hasErrors()) {
            // Chỉ quan tâm lỗi của Lesson (Tên, Order, VideoUrl...)
            // Nếu lỗi thuộc về Lab/Quiz mà form đang ẩn thì bỏ qua (nhưng ở đây ta validate thủ công trong Service rồi)

            // Lấy lỗi đầu tiên để hiển thị
            String msg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi nhập liệu: " + msg);
            return "redirect:/admin/lessons?courseId=" + courseId;
        }

        try {
            // 2. XỬ LÝ GÓI GIÁ (Fix lỗi TransientPropertyValueException)
            if (packageId == null) {
                lesson.setPricePackage(null); // Ngắt liên kết nếu chọn "-- Tất cả --"
            } else {
                // Gán lại object có ID đàng hoàng
                com.codecampus.entity.PricePackage pkg = new com.codecampus.entity.PricePackage();
                pkg.setId(packageId);
                lesson.setPricePackage(pkg);
            }

            // 3. XỬ LÝ LAB (Đóng gói thủ công)
            if (labName != null && !labName.trim().isEmpty()) {
                if (lesson.getLab() == null) lesson.setLab(new com.codecampus.entity.Lab());

                lesson.getLab().setId(labId);
                lesson.getLab().setName(labName);
                lesson.getLab().setLabType(labType);
                lesson.getLab().setDescription(labDesc);
                lesson.getLab().setEvaluationCriteria(labCriteria);
            }

            if (quizIdSelect != null) {
                com.codecampus.entity.Quiz selectedQuiz = quizRepository.findById(quizIdSelect)
                        .orElseThrow(() -> new RuntimeException("Bài kiểm tra đã chọn không tồn tại"));
                lesson.setQuiz(selectedQuiz);
            }
            // 4. XỬ LÝ QUIZ (Đóng gói thủ công)
           else if (quizName != null && !quizName.isEmpty()) {
                if (lesson.getQuiz() == null) lesson.setQuiz(new com.codecampus.entity.Quiz());
                lesson.getQuiz().setId(quizId);
                lesson.getQuiz().setName(quizName);
                lesson.getQuiz().setDurationMinutes(quizDuration);
                lesson.getQuiz().setPassRatePercentage(quizPassRate);
                lesson.getQuiz().setDescription(quizDesc);

                // Set ID Type & Level (Service sẽ findById sau)
                if (quizTypeId != null) {
                    com.codecampus.entity.TestType tt = new com.codecampus.entity.TestType();
                    tt.setId(quizTypeId);
                    lesson.getQuiz().setTestType(tt);
                }
                if (quizLevelId != null) {
                    com.codecampus.entity.QuestionLevel ql = new com.codecampus.entity.QuestionLevel();
                    ql.setId(quizLevelId);
                    lesson.getQuiz().setExamLevel(ql);
                }
            }

            // 5. GỌI SERVICE LƯU
            lessonService.saveLesson(lesson, courseId);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu bài học thành công!");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        return "redirect:/admin/lessons?courseId=" + courseId;
    }

    // 4. TOGGLE STATUS
    @PostMapping("/lessons/toggle/{id}")
    public String toggleStatus(@PathVariable Long id,
                               @RequestParam("courseId") Long courseId,
                               RedirectAttributes redirectAttributes) {
        try {
            lessonService.toggleStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/lessons?courseId=" + courseId;
    }
}