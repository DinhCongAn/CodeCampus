package com.codecampus.controller;

import com.codecampus.entity.*;
import com.codecampus.repository.FeedbackRepository;
import com.codecampus.repository.PricePackageRepository;
import com.codecampus.repository.UserRepository;
import com.codecampus.service.CourseService;
import com.codecampus.service.RegistrationService;
import com.codecampus.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication; // <-- THÊM DÒNG NÀY
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class CourseController {

    @Autowired
    private FeedbackRepository feedbackRepository;
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
            @RequestParam(defaultValue = "6") int size,
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
                                    // Tham số bắt tín hiệu PayOS
                                    @RequestParam(value = "payment", required = false) String payment,
                                    @RequestParam(value = "orderCode", required = false) String orderCode,
                                    Model model,
                                    Authentication authentication) {
        try {
            // 1. Load dữ liệu chung (Sidebar, Course Info, Giá)
            loadSidebarData(model);
            Course course = courseService.getPublishedCourseById(id);
            model.addAttribute("course", course); // Lưu ý: Course này đã có field averageRating và reviewCount
            model.addAttribute("lowestPriceOpt", courseService.getLowestPrice(id));

            // 2. Load danh sách gói giá (cho Modal đăng ký)
            List<PricePackage> packages = pricePackageRepository.findByCourseId(id);
            model.addAttribute("pricePackages", packages);

            // ======================================================
            // [MỚI] 3. LOAD DANH SÁCH FEEDBACK (Lấy 5 cái mới nhất)
            // ======================================================
            Page<Feedback> feedbackPage = feedbackRepository.findByCourseIdOrderByCreatedAtDesc(id, PageRequest.of(0, 5));
            model.addAttribute("feedbacks", feedbackPage.getContent());

            // 4. Logic liên quan đến User đăng nhập
            User currentUser = getCurrentUser(authentication);
            boolean isRegistered = false;

            if (currentUser != null) {
                model.addAttribute("loggedInUser", currentUser);

                // Kiểm tra đã mua khóa học chưa
                isRegistered = registrationService.hasUserRegistered(currentUser.getId(), id);

                // ======================================================
                // [MỚI] 5. KIỂM TRA USER ĐÃ ĐÁNH GIÁ KHÓA NÀY CHƯA
                // ======================================================
                // Để hiển thị form "Sửa đánh giá" hoặc "Viết đánh giá"
                Optional<Feedback> myFeedback = feedbackRepository.findByUserIdAndCourseId(currentUser.getId(), id);
                model.addAttribute("myFeedback", myFeedback.orElse(null));
            }
            model.addAttribute("isRegistered", isRegistered);

            // 6. XỬ LÝ TRẠNG THÁI THANH TOÁN (PayOS Return)
            if (payment != null && currentUser != null) {
                if ("cancel".equals(payment)) {
                    // Nếu hủy -> Xóa đơn hàng pending
                    if (orderCode != null) {
                        registrationService.deletePendingOrder(orderCode, currentUser.getId());
                    }
                    model.addAttribute("errorMessage", "Rất tiếc! Giao dịch của bạn chưa hoàn tất. Hãy thử lại để đăng ký ngay nhé.");
                } else if ("success".equals(payment)) {
                    model.addAttribute("successMessage", "Đăng ký thành công! Bạn có thể vào học ngay.");
                    model.addAttribute("isRegistered", true); // Cập nhật trạng thái ngay lập tức trên UI
                }
            }

            return "course-details";

        } catch (RuntimeException e) {
            // Log lỗi nếu cần thiết
            e.printStackTrace();
            return "redirect:/courses?error=notFound";
        }
    }
}