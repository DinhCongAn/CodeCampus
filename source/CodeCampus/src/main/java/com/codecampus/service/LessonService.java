// src/main/java/com/codecampus/service/LessonService.java
package com.codecampus.service;

import com.codecampus.entity.*;
import com.codecampus.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LessonService {

    @Autowired
    private LessonRepository lessonRepository;
    @Autowired private CourseRepository courseRepository;
    @Autowired private LessonTypeRepository lessonTypeRepository;

    @Autowired private LabRepository labRepository;
    @Autowired private QuizRepository quizRepository;
    @Autowired private TestTypeRepository testTypeRepository;
    @Autowired private QuestionLevelRepository questionLevelRepository;
    /**
     * Lấy một bài học cụ thể bằng ID
     */
    @Transactional(readOnly = true)
    public Lesson getLessonById(Integer lessonId) {
        return lessonRepository.findById(Long.valueOf(lessonId))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài học"));
    }

    /**
     * Lấy toàn bộ danh sách bài học (đã sắp xếp) của một khóa học
     * (Dùng cho sidebar điều hướng)
     */
    @Transactional(readOnly = true)
    public List<Lesson> getActiveLessonsByCourseId(Integer courseId) {
        return lessonRepository.findByCourseIdAndStatusOrderByOrderNumberAsc(courseId, "active");
    }

    // ===== BỔ SUNG PHƯƠNG THỨC NÀY =====
    /**
     * Lấy bài học đầu tiên của khóa học (để redirect)
     */
    @Transactional(readOnly = true)
    public Lesson getFirstLesson(Integer courseId) {
        // Truyền thêm tham số "active" để lọc
        return lessonRepository.findFirstByCourseIdAndStatusOrderByOrderNumberAsc(courseId, "active")
                .orElseThrow(() -> new RuntimeException("Khóa học này chưa có bài học nào được kích hoạt."));
    }

    /**
     * IMPLEMENTATION: Tìm Lesson bằng Lab ID.
     */
    @Transactional(readOnly = true)
    public Lesson findLessonByLabId(Integer labId) {
        return lessonRepository.findByLabId(labId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài học nào chứa Lab ID: " + labId));
    }

    // ===== BỔ SUNG PHƯƠNG THỨC TÌM QUIZ TẠI ĐÂY =====
    /**
     * IMPLEMENTATION: Tìm Lesson bằng Quiz ID.
     * Dùng để xác định bài học (Lesson) cha của một bài Quiz cụ thể.
     */
    @Transactional(readOnly = true)
    public Lesson findLessonByQuizId(Integer quizId) {
        // Giả định bên Repository trả về Optional<Lesson> tương tự như findByLabId
        return lessonRepository.findByQuizId(quizId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài học nào chứa Quiz ID: " + quizId));
    }

    //Admin management lesson

    // Thêm keyword và typeId vào tham số
    public Page<Lesson> getLessonsByCourse(Long courseId, String keyword, Integer typeId, String status, int page, int size) {
        return lessonRepository.findLessonsByCourse(courseId, keyword, typeId, status, PageRequest.of(page, size));
    }

    public Lesson getLessonById(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài học ID: " + id));
    }

    /**
     * LƯU BÀI HỌC (CÓ VALIDATE NGHIỆP VỤ)
     */
    @Transactional
    public void saveLesson(Lesson lesson, Long courseId) {

        // 1. VALIDATE CHUNG (Tên & Thứ tự)

        // Check trùng Thứ tự (Order)
        if (lessonRepository.existsByOrderNumber(courseId, lesson.getOrderNumber(), lesson.getId())) {
            // Logic Auto-Shift (đẩy bài cũ xuống) hoặc Báo lỗi.
            // Ở đây dùng logic đẩy xuống như bài trước:
            lessonRepository.shiftOrdersDown(courseId, lesson.getOrderNumber());
        }

        // [MỚI] Check trùng Tên bài học
        if (lessonRepository.existsByName(courseId, lesson.getName().trim(), lesson.getId())) {
            throw new RuntimeException("Tên bài học '" + lesson.getName() + "' đã tồn tại trong khóa học này.");
        }

        // 2. XỬ LÝ THEO LOẠI BÀI HỌC
        LessonType type = lessonTypeRepository.findById(lesson.getLessonType().getId())
                .orElseThrow(() -> new RuntimeException("Chưa chọn loại bài học"));
        String typeName = type.getName().toLowerCase();

        // --- CASE: LAB ---
        if (typeName.contains("lab")) {
            Lab lab = lesson.getLab();
            if (lab == null || lab.getName() == null || lab.getName().trim().isEmpty()) {
                throw new RuntimeException("Vui lòng nhập Tên bài Lab.");
            }
            if (lab.getId() == null) lab.setCreatedAt(LocalDateTime.now());
            // Set default lab type nếu null
            if (lab.getLabType() == null) lab.setLabType("coding");

            labRepository.save(lab);
            lesson.setLab(lab);

            // Clear data khác
            lesson.setVideoUrl(null);
            lesson.setHtmlContent(null);
            lesson.setQuiz(null);
        }

        // --- CASE: QUIZ ---
        if (typeName.contains("quiz")) {
            Quiz quiz = lesson.getQuiz();

            // 1. Validate cơ bản
            if (quiz == null || quiz.getName() == null || quiz.getName().trim().isEmpty()) {
                throw new RuntimeException("Vui lòng nhập Tên bài kiểm tra.");
            }
            if (quiz.getDurationMinutes() == null || quiz.getDurationMinutes() <= 0) {
                throw new RuntimeException("Thời gian làm bài phải lớn hơn 0.");
            }

            // 2. [FIX LỖI] Gán Course cho Quiz
            // Phải tìm Course từ DB và set vào Quiz thì nó mới lưu course_id
            Course course = courseRepository.findById(courseId.longValue()) // Ép kiểu nếu cần
                    .orElseThrow(() -> new RuntimeException("Khóa học không tồn tại"));
            quiz.setCourse(course);

            // 3. [MỚI] Gán Test Type (Loại kiểm tra)
            if (quiz.getTestType() != null && quiz.getTestType().getId() != null) {
                TestType testType = testTypeRepository.findById(quiz.getTestType().getId())
                        .orElseThrow(() -> new RuntimeException("Loại bài kiểm tra không hợp lệ"));
                quiz.setTestType(testType);
            } else {
                throw new RuntimeException("Vui lòng chọn Loại bài kiểm tra."); // Validate chặt
            }

            // 4. [MỚI] Gán Exam Level (Độ khó)
            if (quiz.getExamLevel() != null && quiz.getExamLevel().getId() != null) {
                QuestionLevel level = questionLevelRepository.findById(quiz.getExamLevel().getId())
                        .orElseThrow(() -> new RuntimeException("Độ khó không hợp lệ"));
                quiz.setExamLevel(level);
            } else {
                throw new RuntimeException("Vui lòng chọn Độ khó."); // Validate chặt
            }

            // 5. Lưu Quiz
            if (quiz.getPassRatePercentage() == null) {
                quiz.setPassRatePercentage(new java.math.BigDecimal("50.00"));
            }
            quizRepository.save(quiz);

            lesson.setQuiz(quiz); // Link lesson -> quiz

            // Clear data thừa
            lesson.setVideoUrl(null);
            lesson.setHtmlContent(null);
            lesson.setLab(null);
        }

        // --- CASE: VIDEO ---
        else if (typeName.contains("video") || typeName.contains("youtube")) {
            if (lesson.getVideoUrl() == null || lesson.getVideoUrl().trim().isEmpty()) {
                throw new RuntimeException("Vui lòng nhập đường dẫn Video.");
            }
            lesson.setLab(null);
            lesson.setQuiz(null);
        }

        // --- CASE: HTML/TEXT ---
        else if (typeName.contains("html") || typeName.contains("text")) {
            if (lesson.getHtmlContent() == null || lesson.getHtmlContent().trim().isEmpty()) {
                throw new RuntimeException("Vui lòng nhập nội dung bài học.");
            }
            lesson.setLab(null);
            lesson.setQuiz(null);
        }

        // --- Default ---
        else {
            lesson.setLab(null);
            lesson.setQuiz(null);
        }

        // 3. THIẾT LẬP COURSE CHA (Giữ nguyên code cũ)
        if (lesson.getId() == null) {
            Course course = courseRepository.findById(courseId.longValue())
                    .orElseThrow(() -> new RuntimeException("Khóa học không tồn tại"));
            lesson.setCourse(course);
            if (lesson.getStatus() == null) lesson.setStatus("active");
        } else {
            Lesson old = getLessonById(lesson.getId());
            lesson.setCourse(old.getCourse());
        }

        lessonRepository.save(lesson);
    }

    public void toggleStatus(Long id) {
        Lesson lesson = getLessonById(id);
        if ("active".equalsIgnoreCase(lesson.getStatus())) {
            lesson.setStatus("inactive");
        } else {
            lesson.setStatus("active");
        }
        lessonRepository.save(lesson);
    }
}