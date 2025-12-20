package com.codecampus.service;

import com.codecampus.entity.Course;
import com.codecampus.entity.Feedback;
import com.codecampus.entity.User;
import com.codecampus.repository.CourseRepository;
import com.codecampus.repository.FeedbackRepository;
import com.codecampus.repository.MyCourseRepository;
import com.codecampus.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FeedbackService {

    @Autowired private FeedbackRepository feedbackRepository;
    @Autowired private CourseRepository courseRepository;
    @Autowired private MyCourseRepository myCourseRepository;
    @Autowired private UserRepository userRepository;

    /**
     * Gửi hoặc cập nhật Feedback
     */
    @Transactional
    public void submitFeedback(Integer userId, Integer courseId, Integer rating, String comment) {
        // 1. Validate Rating
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Đánh giá phải từ 1 đến 5 sao.");
        }

        // 2. Check Enrollment (User đã mua/tham gia khóa học chưa?)
        boolean isEnrolled = myCourseRepository.existsByUserIdAndCourseId(userId, courseId);
        if (!isEnrolled) {
            throw new SecurityException("Bạn chưa tham gia khóa học này nên không thể đánh giá.");
        }

        // 3. Xử lý Feedback (Tạo mới hoặc Update nếu đã tồn tại)
        Feedback feedback = feedbackRepository.findByUserIdAndCourseId(userId, courseId)
                .orElse(new Feedback());

        if (feedback.getId() == null) {
            // New Feedback
            User user = userRepository.findById(userId).orElseThrow();
            Course course = courseRepository.findById(Long.valueOf(courseId)).orElseThrow(); // Course ID của bạn đang là Integer/Long lẫn lộn, hãy cẩn thận ép kiểu
            feedback.setUser(user);
            feedback.setCourse(course);
        }

        feedback.setRating(rating);
        feedback.setComment(comment);

        feedbackRepository.save(feedback);

        // 4. Tính toán lại Average Rating cho Course
        updateCourseRatingStats(courseId);
    }

    /**
     * Xóa Feedback (Chỉ user chính chủ mới được xóa)
     */
    @Transactional
    public void deleteFeedback(Integer feedbackId, Integer userId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));

        if (!feedback.getUser().getId().equals(userId)) {
            throw new SecurityException("Bạn không có quyền xóa đánh giá này.");
        }

        Integer courseId = feedback.getCourse().getId();
        feedbackRepository.delete(feedback);

        // Tính toán lại sau khi xóa
        updateCourseRatingStats(courseId);
    }

    /**
     * Helper: Tính toán lại điểm trung bình và lưu vào bảng Course
     */
    // ... bên trong FeedbackService

    private void updateCourseRatingStats(Integer courseId) {
        // 1. Lấy kết quả dưới dạng List (an toàn hơn trả về Object[] đơn lẻ)
        List<Object[]> results = feedbackRepository.findAverageRatingAndCountByCourseId(courseId);

        Double avg = 0.0;
        Integer count = 0;

        // 2. Kiểm tra và lấy dữ liệu
        if (results != null && !results.isEmpty()) {
            Object[] row = results.get(0); // Lấy dòng đầu tiên

            // Xử lý cột Average Rating (vị trí 0)
            // Dùng 'instanceof Number' để tránh lỗi ClassCastException
            if (row[0] != null && row[0] instanceof Number) {
                avg = ((Number) row[0]).doubleValue();
            }

            // Xử lý cột Count (vị trí 1)
            if (row[1] != null && row[1] instanceof Number) {
                count = ((Number) row[1]).intValue();
            }
        }

        // 3. Làm tròn 1 chữ số thập phân (VD: 4.56 -> 4.6)
        double roundedAvg = Math.round(avg * 10.0) / 10.0;

        // 4. Lưu vào Course
        // Lưu ý: courseRepository.findById nhận Long, nhớ ép kiểu nếu courseId là Integer
        Course course = courseRepository.findById(Long.valueOf(courseId))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học"));

        course.setAverageRating(roundedAvg);
        course.setReviewCount(count);

        courseRepository.save(course);
    }
}