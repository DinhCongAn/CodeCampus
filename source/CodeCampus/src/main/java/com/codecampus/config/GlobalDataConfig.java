package com.codecampus.config;

import com.codecampus.entity.Course;
import com.codecampus.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice // Đánh dấu class này xử lý dữ liệu toàn cục
public class GlobalDataConfig {

    @Autowired
    private CourseService courseService;

    // Hàm này tạo ra biến "footerCourses" dùng được ở mọi file HTML
    @ModelAttribute("footerCourses")
    public List<Course> populateFooterCourses() {
        return courseService.getFeaturedCoursesForFooter();
    }
}