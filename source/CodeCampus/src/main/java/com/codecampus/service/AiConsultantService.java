package com.codecampus.service;

import com.codecampus.entity.*;
import com.codecampus.repository.*;
import com.google.genai.Client;
import com.google.genai.types.HttpOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiConsultantService {

    private static final Logger logger = LoggerFactory.getLogger(AiConsultantService.class);

    private Client geminiClient;

    @Value("${google.api.key}")
    private String apiKey;

    @Autowired private CourseRepository courseRepo;
    @Autowired private UserService userService;
    @Autowired private PricePackageRepository packageRepo;
    @Autowired private BlogRepository blogRepo;
    @Autowired private AiLearningService aiLearningService;

    @Autowired(required = false)
    private MyCourseRepository myCourseRepo;

    // ==========================================
    // INIT GEMINI CLIENT
    // ==========================================
    @PostConstruct
    public void initializeClient() {
        try {
            HttpOptions httpOptions = HttpOptions.builder()
                    .apiVersion("v1beta")
                    .build();

            this.geminiClient = Client.builder()
                    .apiKey(this.apiKey)
                    .httpOptions(httpOptions)
                    .build();

            logger.info("✅ AiConsultantService: Gemini Client khởi tạo thành công với API v1beta");

        } catch (Exception e) {
            logger.error("❌ Lỗi khởi tạo Gemini Client: {}", e.getMessage(), e);
        }
    }

    // ==========================================
    // MAIN METHOD — GUEST + USER
    // ==========================================
    public String getConsultation(String message, Integer userId) {

        if (geminiClient == null)
            return "AI đang khởi động… đợi xíu nha 😎";

        String courseC = buildCourseContext();
        String priceC  = buildPricePackageContext();
        String blogC   = buildBlogContext();
        String userC   = buildUserContext();

        if (courseC.equals("Chưa có khóa active.")) {
            return "Hiện tại hệ thống chưa có khóa học nào để tư vấn. Bạn thử hỏi chủ đề khác nha!";
        }

        String prompt = """
                Bạn là CodeCampus AI – trợ lý học tập của nền tảng.
                Dựa trên dữ liệu hệ thống bên dưới để trả lời.

                [Khóa học]
                %s
                (LƯU Ý: Nếu mục Khóa học trống hoặc không có dữ liệu, tuyệt đối KHÔNG được tư vấn bất kỳ khóa học nào.)

                [Gói giá]
                %s

                [Blog]
                %s

                [Người dùng]
                %s

                Câu hỏi: "%s"

                Quy tắc trả lời:
                - Tiếng Việt
                - Ngắn gọn, thân thiện, vibe Gen Z 😎
                - Nếu user chưa đăng nhập → tư vấn chung
                - User đã login → cá nhân hóa dựa trên tiến độ học
                - Nếu câu hỏi ngoài CNTT → từ chối nhẹ nhàng
                """.formatted(courseC, priceC, blogC, userC, message);

        return aiLearningService.callGeminiApi(prompt, "getConsultation");
    }


    // ==========================================
    // CONTEXT GENERATORS
    // ==========================================

    private String buildCourseContext() {
        List<Course> list = courseRepo.findCourseByStatus("ACTIVE");

        if (list == null || list.isEmpty()) {
            return "Chưa có khóa active.";
        }

        StringBuilder sb = new StringBuilder();
        for (Course c : list) {
            sb.append("- ").append(c.getName())
                    .append(" (")
                    .append(c.getCategory() != null ? c.getCategory().getName() : "Không phân loại")
                    .append(")\n");
        }
        return sb.toString();
    }


    private String buildPricePackageContext() {
        List<PricePackage> list = packageRepo.findAll();

        if (list.isEmpty()) return "Không có gói.";

        StringBuilder sb = new StringBuilder();
        for (PricePackage p : list) {
            sb.append("- ")
                    .append(p.getName())
                    .append(" – ")
                    .append(p.getCourse().getName())
                    .append(": ")
                    .append(p.getSalePrice() != null ? p.getSalePrice() : p.getListPrice())
                    .append("đ\n");
        }
        return sb.toString();
    }


    private String buildBlogContext() {
        List<Blog> list = blogRepo.findAll();

        if (list.isEmpty()) return "(Không có blog)";

        StringBuilder sb = new StringBuilder();
        list.stream().limit(3).forEach(b -> sb.append("- ").append(b.getTitle()).append("\n"));
        return sb.toString();
    }


    // ==========================================
    // ⭐ BUILD USER CONTEXT — CHUẨN NHẤT
    // ==========================================
    private String buildUserContext() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "(Guest — không có dữ liệu người dùng)";
        }

        Object principal = auth.getPrincipal();
        String email = null;

        // 1️⃣ Form Login (email/password)
        if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername(); // username = email
        }

        // 2️⃣ Google Login
        else if (principal instanceof OAuth2User oauth) {
            email = oauth.getAttribute("email");
        }

        if (email == null) {
            return "(Guest — không có dữ liệu người dùng)";
        }

        // Lấy User entity
        User user = userService.findUserByEmail(email);
        if (user == null) {
            return "(Không tìm thấy thông tin người dùng)";
        }

        if (myCourseRepo == null) {
            return "(Không thể lấy danh sách khóa học — repo null)";
        }

        List<MyCourse> list = myCourseRepo.findByUserId(user.getId());
        if (list.isEmpty()) {
            return "(User chưa đăng ký khóa học nào)";
        }

        StringBuilder sb = new StringBuilder();
        for (MyCourse m : list) {
            sb.append("- ")
                    .append(m.getCourse().getName())
                    .append(" (Tiến độ: ")
                    .append(m.getProgressPercent())
                    .append("%)\n");
        }

        return sb.toString();
    }

}
