package com.codecampus.config;

import com.codecampus.entity.Course;
import com.codecampus.service.CourseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class GlobalDataConfig {

    @Autowired
    private CourseService courseService;

    @ModelAttribute
    public void ensureSessionExists(HttpServletRequest request) {
        // Ép buộc tạo Session ngay khi bắt đầu Request
        // Điều này đảm bảo CSRF Token có sẵn trước khi render HTML
        HttpSession session = request.getSession(true);
    }
    // -----------------------------------

    @ModelAttribute("footerCourses")
    public List<Course> populateFooterCourses() {
        return courseService.getFeaturedCoursesForFooter();
    }
}