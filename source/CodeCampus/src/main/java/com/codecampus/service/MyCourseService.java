package com.codecampus.service;

import com.codecampus.entity.*;
import com.codecampus.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class MyCourseService {

    @Autowired
    private MyCourseRepository myCourseRepository;

    @Autowired
    private UserLessonProgressRepository progressRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private CourseRepository courseRepository; // Bổ sung để đảm bảo lấy được course nếu cần

    /**
     * Kiểm tra xem user có được phép học khóa này không (dựa trên Registration)
     */
    public boolean isUserEnrolled(Integer userId, Integer courseId) {
        // Giả định RegistrationRepository có hàm findByUserIdAndCourseId
        Registration reg = registrationRepository.findByUserIdAndCourseId(userId, courseId);
        return reg != null && "COMPLETED".equalsIgnoreCase(reg.getStatus());
    }

    /**
     * Hàm quan trọng: Cập nhật tiến độ khi user học xong 1 bài
     */
    @Transactional
    public void updateProgress(Integer userId, Integer courseId, Integer lessonId) {
        // 1. CẬP NHẬT BẢNG CHI TIẾT (USER_LESSON_PROGRESS)
        UserLessonProgress progress = progressRepository.findByUserIdAndLessonId(userId, lessonId);

        if (progress == null) {
            // Nếu chưa từng học bài này, tạo mới
            User user = new User(); user.setId(userId);
            Lesson lesson = new Lesson(); lesson.setId(Long.valueOf(lessonId));
            Course course = new Course(); course.setId(courseId);

            progress = UserLessonProgress.builder()
                    .user(user)
                    .lesson(lesson)
                    .course(course)
                    .isCompleted(true) // Đánh dấu đã học xong
                    .completedAt(LocalDateTime.now())
                    .lastAccessed(LocalDateTime.now())
                    .build();
        } else {
            // Nếu đã tồn tại, cập nhật thời gian và trạng thái
            if (!progress.isCompleted()) {
                progress.setCompleted(true);
                progress.setCompletedAt(LocalDateTime.now());
            }
            progress.setLastAccessed(LocalDateTime.now());
        }
        progressRepository.save(progress);

        // 2. TÍNH TOÁN LẠI % VÀ CẬP NHẬT BẢNG TỔNG (MY_COURSES)
        recalculateCourseProgress(userId, courseId, lessonId);
    }

    /**
     * Tính toán lại % tổng thể của khóa học
     */
    private void recalculateCourseProgress(Integer userId, Integer courseId, Integer currentLessonId) {
        // Tổng số bài học ACTIVE trong khóa
        // Lưu ý: Repository cần có hàm countByCourseIdAndStatus
        long totalLessons = lessonRepository.countByCourseIdAndStatus(courseId, "active");

        // Tránh lỗi chia cho 0
        if (totalLessons == 0) totalLessons = 1;

        // Tổng số bài User đã hoàn thành
        long completedCount = progressRepository.countCompletedLessons(userId, courseId);

        // Tính %
        double percent = ((double) completedCount / totalLessons) * 100.0;

        // Đảm bảo không vượt quá 100%
        if (percent > 100.0) percent = 100.0;

        // 3. Lưu vào bảng MY_COURSES
        MyCourse myCourse = myCourseRepository.findByUserIdAndCourseId(userId, courseId);

        if (myCourse == null) {
            myCourse = new MyCourse();
            User u = new User(); u.setId(userId);
            Course c = new Course(); c.setId(courseId);
            myCourse.setUser(u);
            myCourse.setCourse(c);
            myCourse.setStatus("in_progress");
        }

        myCourse.setProgressPercent(percent);

        // Cập nhật bài học cuối cùng user đang xem để lần sau vào nút "Học tiếp"
        Lesson lastLesson = new Lesson();
        lastLesson.setId(Long.valueOf(currentLessonId));
        myCourse.setLastLesson(lastLesson);

        myCourse.setLastAccessed(LocalDateTime.now());

        // Nếu đạt 100% thì đổi trạng thái khóa học
        if (percent == 100.0) {
            myCourse.setStatus("completed");
        }

        myCourseRepository.save(myCourse);
    }

    /**
     * Lấy thông tin tiến độ tổng quan để hiển thị ra ngoài danh sách
     */
    public MyCourse getMyCourse(Integer userId, Integer courseId) {
        return myCourseRepository.findByUserIdAndCourseId(userId, courseId);
    }
}