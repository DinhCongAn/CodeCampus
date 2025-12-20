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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class HomeController {

    @Autowired private SliderRepository sliderRepository;
    @Autowired private BlogRepository blogRepository;
    @Autowired private CourseRepository courseRepository;
    @Autowired private MyCourseRepository myCourseRepository;

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

        // Logic Marketing (Tùy chọn): Nếu tỉ lệ thật quá thấp (vd: 5%),
        // bạn có thể muốn lấy "Tiến độ trung bình" thay vì "Tỉ lệ hoàn thành" để số đẹp hơn.
        // Nếu muốn số đẹp, có thể dùng công thức khác, nhưng ở đây tôi để logic thật.

        model.addAttribute("completionRate", completionRate);

        return "home"; // Trả về file home.html
    }
}