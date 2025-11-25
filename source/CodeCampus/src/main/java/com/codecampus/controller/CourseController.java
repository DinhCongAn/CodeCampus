package com.codecampus.controller;

import com.codecampus.entity.Course;
import com.codecampus.entity.CourseCategory;
import com.codecampus.entity.PricePackage; // BỔ SUNG
import com.codecampus.entity.User;
import com.codecampus.repository.PricePackageRepository;
import com.codecampus.repository.UserRepository;
import com.codecampus.service.CourseService;
import com.codecampus.service.RegistrationService;
import com.codecampus.service.UserService;
import org.springframework.security.core.Authentication; // <-- THÊM DÒNG NÀY
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class CourseController {

    private final CourseService courseService;
    private final PricePackageRepository pricePackageRepository; // THAY ĐỔI
    private final UserService userService;
    private final RegistrationService registrationService;

    // THAY ĐỔI: Xóa PricePackageService, thay bằng PricePackageRepository
    public CourseController(CourseService courseService,
                            PricePackageRepository pricePackageRepository,
                            UserService userService,
                            RegistrationService registrationService) {
        this.courseService = courseService;
        this.pricePackageRepository = pricePackageRepository; // THAY ĐỔI
        this.userService = userService;
        this.registrationService = registrationService;
    }

    // --- Helper lấy User (Giữ nguyên) ---
    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;
        String email = authentication.getName();
        try {
            return userService.findUserByEmail(email);
        } catch (Exception e) {
            return null;
        }
    }

    // (loadSidebarData và showCourseList giữ nguyên)
    private void loadSidebarData(Model model) {
        List<CourseCategory> categories = courseService.getAllActiveCategories();
        List<Course> featuredCourses = courseService.getFeaturedCourses();
        model.addAttribute("courseCategories", categories);
        model.addAttribute("featuredCourses", featuredCourses);
    }

    /**
     * Màn hình 10: Hiển thị DANH SÁCH KHÓA HỌC (List)
     */
    @GetMapping("/courses")
    public String showCourseList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            Model model) {

        loadSidebarData(model);
        Page<Course> coursePage = courseService.getPublishedCourses(page, size, keyword, categoryId);
        model.addAttribute("coursePage", coursePage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("courseService", courseService);

        return "course-list";
    }

    /**
     * Màn hình 11: Hiển thị CHI TIẾT KHÓA HỌC (Details)
     */
    @GetMapping("/courses/{id}")
    public String showCourseDetails(@PathVariable("id") Integer id,
                                    // [MỚI] Thêm tham số để bắt tín hiệu từ PayOS trả về
                                    @RequestParam(value = "payment", required = false) String payment,
                                    @RequestParam(value = "orderCode", required = false) String orderCode,
                                    Model model,
                                    Authentication authentication) {
        try {
            loadSidebarData(model);
            Course course = courseService.getPublishedCourseById(id);
            model.addAttribute("course", course);
            model.addAttribute("lowestPriceOpt", courseService.getLowestPrice(id));

            // (Pop-up cần danh sách gói giá)
            List<PricePackage> packages = pricePackageRepository.findByCourseId(id);
            model.addAttribute("pricePackages", packages);

            // ===== Logic kiểm tra đăng ký =====
            User currentUser = getCurrentUser(authentication);
            boolean isRegistered = false;

            if (currentUser != null) {
                model.addAttribute("loggedInUser", currentUser);
                isRegistered = registrationService.hasUserRegistered(currentUser.getId(), id);
            }
            model.addAttribute("isRegistered", isRegistered);

            // ===== XỬ LÝ TRẠNG THÁI THANH TOÁN =====
            if (payment != null && currentUser != null) {
                if ("cancel".equals(payment)) {
                    // 1. NẾU HỦY -> GỌI SERVICE XÓA ĐƠN HÀNG NGAY LẬP TỨC
                    if (orderCode != null) {
                        registrationService.deletePendingOrder(orderCode, currentUser.getId());
                    }

                    model.addAttribute("errorMessage", "Rất tiếc! Giao dịch của bạn chưa hoàn tất. Hãy thử lại để đăng ký ngay nhé.");
                } else if ("success".equals(payment)) {
                    model.addAttribute("successMessage", "Đăng ký thành công! Bạn có thể vào học ngay.");
                    model.addAttribute("isRegistered", true);
                }
            }
            // ============================================

            return "course-details";
        } catch (RuntimeException e) {
            return "redirect:/courses?error=notFound";
        }
    }
}