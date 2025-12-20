package com.codecampus.service;

import com.codecampus.entity.*;
import com.codecampus.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Objects;

@Service
public class FeedbackService {

    @Autowired private FeedbackRepository feedbackRepository;
    @Autowired private CourseRepository courseRepository;
    @Autowired private MyCourseRepository myCourseRepository;
    @Autowired private UserRepository userRepository;

    // Đường dẫn lưu file (Cần đảm bảo thư mục này tồn tại hoặc code sẽ tự tạo)
    private final String UPLOAD_DIR = "src/main/resources/static/uploads/feedback/";

    /**
     * Gửi hoặc cập nhật Feedback (Có hỗ trợ đính kèm ảnh)
     */
    @Transactional
    public void submitFeedback(Integer userId, Integer courseId, Integer rating, String comment, MultipartFile file) throws IOException {

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
            User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
            Course course = courseRepository.findById(Long.valueOf(courseId)).orElseThrow(() -> new RuntimeException("Course not found"));

            feedback.setUser(user);
            feedback.setCourse(course);
        }

        feedback.setRating(rating);
        feedback.setComment(comment);

        // 4. XỬ LÝ FILE ĐÍNH KÈM (MỚI BỔ SUNG)
        if (file != null && !file.isEmpty()) {
            // Lấy tên file gốc và làm sạch
            String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

            // Tạo tên file duy nhất: currentTimeMillis_tenfile.jpg
            String uniqueFileName = System.currentTimeMillis() + "_" + originalFileName;

            // Kiểm tra và tạo thư mục nếu chưa tồn tại
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Lưu file vào ổ cứng
            try (var inputStream = file.getInputStream()) {
                Files.copy(inputStream, uploadPath.resolve(uniqueFileName), StandardCopyOption.REPLACE_EXISTING);
            }

            // Tạo đối tượng Attachment (Sử dụng Builder từ Lombok ở Entity FeedbackAttachment)
            FeedbackAttachment attachment = FeedbackAttachment.builder()
                    .fileName(originalFileName)
                    .fileUrl("/uploads/feedback/" + uniqueFileName) // Đường dẫn web truy cập
                    .fileType(file.getContentType())
                    .feedback(feedback) // Gán quan hệ 2 chiều
                    .build();

            // Thêm vào danh sách attachments của Feedback
            // (Đảm bảo entity Feedback đã có hàm addAttachment như hướng dẫn trước)
            feedback.addAttachment(attachment);
        }

        // 5. Lưu Feedback (Cascade sẽ tự động lưu Attachment)
        feedbackRepository.save(feedback);

        // 6. Tính toán lại Average Rating cho Course
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

        // TODO: Nếu muốn xóa triệt để, có thể thêm logic xóa file ảnh trong ổ cứng ở đây
        // Nhưng Hibernate sẽ tự xóa record attachment trong DB nhờ CascadeType.REMOVE

        feedbackRepository.delete(feedback);

        // Tính toán lại sau khi xóa
        updateCourseRatingStats(courseId);
    }

    /**
     * Helper: Tính toán lại điểm trung bình và lưu vào bảng Course
     */
    private void updateCourseRatingStats(Integer courseId) {
        // 1. Lấy kết quả dưới dạng List
        List<Object[]> results = feedbackRepository.findAverageRatingAndCountByCourseId(courseId);

        Double avg = 0.0;
        Integer count = 0;

        // 2. Kiểm tra và lấy dữ liệu an toàn
        if (results != null && !results.isEmpty()) {
            Object[] row = results.get(0);

            // Xử lý cột Average Rating
            if (row[0] != null && row[0] instanceof Number) {
                avg = ((Number) row[0]).doubleValue();
            }

            // Xử lý cột Count
            if (row[1] != null && row[1] instanceof Number) {
                count = ((Number) row[1]).intValue();
            }
        }

        // 3. Làm tròn 1 chữ số thập phân
        double roundedAvg = Math.round(avg * 10.0) / 10.0;

        // 4. Lưu vào Course
        Course course = courseRepository.findById(Long.valueOf(courseId))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học"));

        course.setAverageRating(roundedAvg);
        course.setReviewCount(count);

        courseRepository.save(course);
    }
}