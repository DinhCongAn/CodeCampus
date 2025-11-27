package com.codecampus.service;

import com.codecampus.entity.*;
import com.codecampus.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class QuizService {

    @Autowired private QuizRepository quizRepository;
    @Autowired private CourseRepository courseRepository;
    @Autowired private TestTypeRepository testTypeRepository;
    @Autowired private QuestionLevelRepository questionLevelRepository;
    @Autowired private LessonRepository lessonRepository;

    @Transactional(readOnly = true)
    public Quiz findQuizById(Integer quizId) {
        // .orElseThrow() sẽ tự ném 500 nếu không tìm thấy
        return quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Quiz với ID: " + quizId));
    }

    // 1. Lấy danh sách
    public Page<Quiz> getQuizzesAdmin(String keyword, Integer courseId, Integer typeId, int page, int size) {
        return quizRepository.findQuizzesAdmin(keyword, courseId, typeId, PageRequest.of(page, size));
    }

    public Quiz getQuizById(Integer id) {
        return quizRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy Quiz"));
    }

    // 2. Lấy số lượng câu hỏi
    public Integer getQuestionCount(Integer quizId) {
        return quizRepository.countQuestionsByQuizId(quizId);
    }

    // 3. Check quyền Sửa/Xóa (Requirement: Only when no test taken)
    public boolean canEditOrDelete(Integer quizId) {
        // Nếu đã có lượt làm bài (Attempt) -> Không được sửa/xóa
        return !quizRepository.hasAttempts(quizId);
    }

    // 4. Xóa Quiz
    @Transactional
    public void deleteQuiz(Integer id) {
        // 1. Check quyền (Giữ nguyên)
        if (!canEditOrDelete(id)) {
            throw new RuntimeException("Không thể xóa bài kiểm tra này vì đã có học viên làm bài.");
        }

        // 2. [SỬA Ở ĐÂY] Gọi hàm mới trả về List
        List<Lesson> lessonsUsingQuiz = lessonRepository.findAllByQuizId(id);

        for (Lesson lesson : lessonsUsingQuiz) {
            lesson.setQuiz(null);
            lesson.setStatus("inactive");
            lessonRepository.save(lesson);
        }

        // 3. Xóa Quiz
        quizRepository.deleteById(id);
    }

    // 5. Lưu Quiz (Logic update/create)
    /**
     * HÀM LƯU QUIZ (FULL VALIDATE)
     */
    @Transactional
    public void saveQuiz(Quiz quiz, Integer courseId, Integer typeId, Integer levelId) {

        // 1. VALIDATE LOGIC SỬA ĐỔI (Requirement 33)
        // Nếu là Sửa (ID != null) và Đã có người thi -> CHẶN
        if (quiz.getId() != null) {
            boolean hasAttempts = quizRepository.hasAttempts(quiz.getId());
            if (hasAttempts) {
                throw new RuntimeException("Không thể chỉnh sửa bài kiểm tra này vì đã có học viên làm bài.");
            }
        }

        // 2. VALIDATE INPUT CƠ BẢN
        if (quiz.getDurationMinutes() == null || quiz.getDurationMinutes() <= 0) {
            throw new RuntimeException("Thời gian làm bài phải lớn hơn 0 phút.");
        }
        if (quiz.getPassRatePercentage() != null) {
            double rate = quiz.getPassRatePercentage().doubleValue();
            if (rate < 0 || rate > 100) {
                throw new RuntimeException("Tỷ lệ đạt phải từ 0% đến 100%.");
            }
        }

        // 3. CHECK TRÙNG TÊN (Trong cùng 1 khóa học)
        // Cắt khoảng trắng thừa
        String cleanName = quiz.getName().trim();
        quiz.setName(cleanName);

        boolean isDuplicate = quizRepository.existsByNameAndCourse(cleanName, courseId, quiz.getId());
        if (isDuplicate) {
            throw new RuntimeException("Tên bài kiểm tra '" + cleanName + "' đã tồn tại trong khóa học này.");
        }

        // 4. THIẾT LẬP QUAN HỆ (Mapping ID sang Entity)
        // Gán Course
        Course course = courseRepository.findById(Long.valueOf(courseId)) // Ép kiểu nếu cần
                .orElseThrow(() -> new RuntimeException("Vui lòng chọn Môn học."));
        quiz.setCourse(course);

        // Gán Type
        TestType type = testTypeRepository.findById(typeId)
                .orElseThrow(() -> new RuntimeException("Vui lòng chọn Loại bài kiểm tra."));
        quiz.setTestType(type);

        // Gán Level
        QuestionLevel level = questionLevelRepository.findById(levelId)
                .orElseThrow(() -> new RuntimeException("Vui lòng chọn Độ khó/Cấp độ."));
        quiz.setExamLevel(level);

        // 5. LƯU
        quizRepository.save(quiz);
    }
}