package com.codecampus.controller;

import com.codecampus.dto.MyCourseDto;
import com.codecampus.entity.CourseCategory;
import com.codecampus.entity.MyCourse;
import com.codecampus.entity.Registration;
import com.codecampus.entity.User;
import com.codecampus.service.CourseService;
import com.codecampus.service.MyCourseService;
import com.codecampus.service.RegistrationService;
import com.codecampus.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class MyCoursesController {

    @Autowired
    private UserService userService;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private MyCourseService myCourseService;

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String email = authentication.getName();
        try {
            return userService.findUserByEmail(email);
        } catch (Exception e) {
            return null;
        }
    }

    private void loadSidebarData(Model model) {
        List<CourseCategory> categories = courseService.getAllActiveCategories();
        model.addAttribute("courseCategories", categories);
    }

    @GetMapping("/my-courses")
    public String showMyCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            Model model,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) {
            return "redirect:/login";
        }

        loadSidebarData(model);

        List<Registration> myRegistrations = registrationService.getCoursesByUserId(
                currentUser.getId(),
                keyword,
                categoryId
        );

        List<MyCourseDto> myCoursesDtoList = new ArrayList<>();

        for (Registration reg : myRegistrations) {
            Double progressPercent = 0.0;

            // SỬA TẠI ĐÂY: Đổi Integer thành Long để khớp với ID của Lesson
            Long lastLessonId = null;

            if ("COMPLETED".equalsIgnoreCase(reg.getStatus())) {
                MyCourse myCourse = myCourseService.getMyCourse(currentUser.getId(), reg.getCourse().getId());

                if (myCourse != null) {
                    progressPercent = myCourse.getProgressPercent();

                    // Hibernate trả về Long, gán vào biến Long -> Hết lỗi
                    if (myCourse.getLastLesson() != null) {
                        lastLessonId = myCourse.getLastLesson().getId();
                    }
                }
            }

            // Truyền biến Long vào DTO
            myCoursesDtoList.add(new MyCourseDto(reg, progressPercent, lastLessonId));
        }

        model.addAttribute("myCourses", myCoursesDtoList);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);

        return "my-courses";
    }
}