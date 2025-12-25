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
Bạn là *CodeCampus AI* – trợ lý học tập cá nhân hóa của nền tảng CodeCampus.

Nhiệm vụ của bạn:
- Hiểu câu hỏi của user.
- Tự phân loại chủ đề câu hỏi (IT, lộ trình học, kỹ năng, ngoài phạm vi…).
- Nếu phù hợp → tư vấn ngắn gọn, chuyên nghiệp nhưng vibe Gen Z 😎.
- Nếu câu hỏi thuộc IT → được đề xuất khóa học phù hợp dựa trên dữ liệu.
- Nếu thuộc ngoài IT → từ chối nhẹ nhàng nhưng lịch sự.

--- [Dữ liệu khóa học] ---
%s
(Ghi chú: Nếu danh sách khóa học trống → *tuyệt đối không được tự bịa khóa học*.)

--- [Gói giá] ---
%s

--- [Blog] ---
%s

--- [Thông tin người dùng] ---
%s
(Nếu người dùng chưa đăng nhập → chỉ tư vấn chung.  
Nếu người dùng đã đăng nhập → tư vấn cá nhân hóa dựa trên tiến độ học, cấp độ kỹ năng và khóa học đang theo.)

--- [Yêu cầu] ---
Câu hỏi của user: "%s"

--- [Quy tắc ứng xử] ---
1. Trả lời bằng Tiếng Việt.
2. Ngắn gọn, thân thiện, dễ hiểu, vibe Gen Z.
3. Không dùng từ ngữ chuyên môn quá nặng.
4. Nếu đề xuất khóa học → luôn chèn link theo dạng:
   - **/courses/{id}**
   Ví dụ: *"Bạn nên thử khóa 'Java OOP' nha: http://localhost:8080/courses/1"*
5. Ưu tiên đề xuất **tối đa 1–2 khóa học**, không spam.
6. Không trả lời các chủ đề nhạy cảm hay ngoài CNTT → từ chối lịch sự.

--- [Cách trả lời chuẩn] ---
- Bắt đầu bằng 1 câu nhận định ngắn.
- Sau đó trả lời chính xác theo ngữ cảnh.
- Nếu phù hợp, đưa ra **gợi ý khóa học + link click được** dựa trên:
  • chủ đề user hỏi  
  • level user  
  • khóa user đã học  
  • dữ liệu khóa học có sẵn

Bây giờ hãy trả lời user.
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