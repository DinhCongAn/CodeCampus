package com.codecampus.controller;

import com.codecampus.entity.Course;
import com.codecampus.repository.BlogRepository;
import com.codecampus.repository.CourseRepository;
import com.codecampus.repository.MyCourseRepository;
import com.codecampus.repository.SliderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.codecampus.repository.FeedbackRepository; // <--- Import mới
import org.springframework.data.domain.PageRequest;  // <--- Import mới

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class HomeController {

    @Autowired private SliderRepository sliderRepository;
    @Autowired private BlogRepository blogRepository;
    @Autowired private CourseRepository courseRepository;
    @Autowired private MyCourseRepository myCourseRepository;
    @Autowired private FeedbackRepository feedbackRepository; // <--- Inject thêm

    @GetMapping("/home")
    public String showHomePage(Model model) {

        // ... (Phần 1: Slider & Blog giữ nguyên) ...
        model.addAttribute("sliders", sliderRepository.findByStatus("active"));
        model.addAttribute("hotPosts", blogRepository.findTop4ByStatusAndIsFeaturedTrueOrderByPublishedAtDesc("published"));
        model.addAttribute("latestPostsForSidebar", blogRepository.findTop5ByStatusOrderByCreatedAtDesc("published"));


        // ... (Phần 2: Featured Courses giữ nguyên) ...
        List<Course> featuredCourses = courseRepository.findByIsFeaturedAndStatus(true, "ACTIVE");
        featuredCourses.forEach(course -> {
            long studentCount = myCourseRepository.countByCourse_Id(Long.valueOf(course.getId()));
            course.setStudentCount(studentCount);
        });
        model.addAttribute("featuredCourses", featuredCourses);


        // ============================================================
        // 3. SYSTEM STATS (ĐÃ CHỈNH SỬA)
        // ============================================================

        // A. Tổng học viên
        long totalStudents = myCourseRepository.countDistinctStudents();
        model.addAttribute("totalStudents", totalStudents);

        // B. Học viên hoạt động hôm nay
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        // Đổi tên biến thành activeToday cho đúng bản chất dữ liệu (vì dùng lastAccessed)
        long todayStudents = myCourseRepository.countActiveStudentsToday(startOfToday);
        model.addAttribute("todayStudents", todayStudents);

        // C. Top Course Enroll (Xử lý an toàn null/rỗng)
        List<Long> topCounts = myCourseRepository.findTopCourseEnrollmentCounts();
        long topCourseEnroll = topCounts.isEmpty() ? 0 : topCounts.get(0);
        model.addAttribute("topCourseEnroll", topCourseEnroll);

        // D. Tỷ lệ hoàn thành (Completion Rate)
        long totalEnrollments = myCourseRepository.count(); // Tổng số lượt học (dùng hàm có sẵn của JPA)
        long completedCourses = myCourseRepository.countCompletedCourses(); // Số lượt đã xong

        int completionRate = 0;
        if (totalEnrollments > 0) {
            // Ép kiểu double để chia ra số thập phân, sau đó nhân 100 và ép về int
            completionRate = (int) (((double) completedCourses / totalEnrollments) * 100);
        }

        model.addAttribute("completionRate", completionRate);

        // Lấy 3 feedback tốt nhất để hiển thị
        model.addAttribute("feedbacks",
                feedbackRepository.findTopFeedbacks(PageRequest.of(0, 6)));

        model.addAttribute("pageTitle", "Trang chủ | CodeCampus");

        return "home"; // Trả về file home.html
    }
}