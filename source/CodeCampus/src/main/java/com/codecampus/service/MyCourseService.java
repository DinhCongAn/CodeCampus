package com.codecampus.service;

import com.codecampus.entity.Course;
import com.codecampus.entity.Lesson;
import com.codecampus.entity.MyCourse;
import com.codecampus.entity.User;
import com.codecampus.repository.MyCourseRepository;
// import com.codecampus.repository.RegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MyCourseService {

    // @Autowired
    // private RegistrationRepository registrationRepository;

    @Autowired
    private MyCourseRepository myCourseRepository;

    @Autowired
    private LessonService lessonService; // Dùng lại service của bạn

    @Transactional(readOnly = true)
    public boolean isUserEnrolled(Integer userId, Integer courseId) {
        // TODO: Triển khai logic kiểm tra đăng ký (bảng registrations)
        // return registrationRepository.existsByUserIdAndCourseIdAndStatus(userId, courseId, "valid");
        return true; // Tạm thời
    }

    @Transactional
    public void updateProgress(Integer userId, Integer courseId, Long lessonId) {
        // Tìm bản ghi tiến độ
        MyCourse myCourse = myCourseRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseGet(() -> {
                    // Nếu chưa có, tạo mới (cần User/Course entities)
                    MyCourse newProgress = new MyCourse();
                    // ... (cần set User và Course, tạm bỏ qua để đơn giản)
                    // ...
                    return newProgress;
                });

        // Lấy tổng số bài học
        List<Lesson> allLessons = lessonService.getLessonsByCourseId(courseId);
        int totalLessons = allLessons.size();
        if (totalLessons == 0) return; // Không làm gì nếu khóa học không có bài học

        // Tìm thứ tự bài học hiện tại
        int currentLessonIndex = -1;
        for (int i = 0; i < allLessons.size(); i++) {
            if (allLessons.get(i).getId().equals(lessonId)) {
                currentLessonIndex = i;
                break;
            }
        }

        // Tính toán % (chỉ cập nhật nếu tiến độ mới > tiến độ cũ)
        // (currentLessonIndex + 1) / totalLessons
        BigDecimal newProgressPercent = new BigDecimal(currentLessonIndex + 1)
                .divide(new BigDecimal(totalLessons), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100));

        if (myCourse.getProgressPercent() == null || newProgressPercent.compareTo(myCourse.getProgressPercent()) > 0) {
            myCourse.setProgressPercent(newProgressPercent);
        }

        myCourse.setLastLessonId(lessonId);
        myCourse.setLastAccessed(LocalDateTime.now());

        // Cần gán User và Course nếu là bản ghi mới...
        // myCourseRepository.save(myCourse); // Tạm comment vì thiếu User/Course
        System.out.println("Cập nhật tiến độ: " + newProgressPercent + "%");
    }
}