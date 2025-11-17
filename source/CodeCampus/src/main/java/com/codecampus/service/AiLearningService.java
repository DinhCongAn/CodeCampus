package com.codecampus.service;

import com.codecampus.entity.Lesson; // <-- THÊM MỚI
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.Schema;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired; // <-- THÊM MỚI
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.CompletableFuture;

@Service
public class AiLearningService { // <-- Đổi tên thành AiLearningService

    private static final Logger logger = LoggerFactory.getLogger(AiLearningService.class);

    private Client geminiClient;

    @Value("${google.api.key}")
    private String apiKey;

    @Autowired // <-- THÊM MỚI
    private LessonService lessonService; // <-- Dịch vụ để lấy nội dung bài học

    /** Khởi tạo Gemini Client (giữ nguyên code của bạn) */
    @PostConstruct
    public void initializeClient() {
        try {
            HttpOptions httpOptions = HttpOptions.builder()
                    .apiVersion("v1beta") // API v1beta hỗ trợ models mới hơn
                    .build();
            this.geminiClient = Client.builder()
                    .apiKey(this.apiKey)
                    .httpOptions(httpOptions)
                    .build();
            logger.info("✅ AiLearningService: Gemini Client khởi tạo thành công với API v1beta");
        } catch (Exception e) {
            logger.error("❌ Lỗi khởi tạo Gemini Client: {}", e.getMessage(), e);
        }
    }

    // --- CÁC TÍNH NĂNG MỚI CHO MÀN HÌNH 15 ---

    /**
     * MỚI (Cho MH-15 Chat): Chat hỗ trợ học tập (thay thế chatWithContext)
     * Lấy ngữ cảnh bài học thực tế để trả lời.
     */
    public String getContextualAnswer(Long lessonId, String userQuestion) {
        logger.info("AiLearningService: Xử lý chat với ngữ cảnh cho lessonId: {}", lessonId);

        // 1. Lấy ngữ cảnh thật
        Lesson lesson;
        try {
            lesson = lessonService.getLessonById(lessonId.intValue());
        } catch (Exception e) {
            logger.error("❌ Lỗi getContextualAnswer (không tìm thấy lesson): {}", e.getMessage());
            return "Lỗi: Không tìm thấy bài học để lấy ngữ cảnh.";
        }

        String lessonContent = stripHtml(lesson.getHtmlContent()); // Làm sạch HTML

        // 2. Xây dựng Prompt
        String prompt = String.format(
                "Bạn là một trợ giảng AI, chỉ được phép trả lời dựa vào nội dung được cung cấp.\n" +
                        "--- BẮT ĐẦU NỘI DUNG BÀI HỌC (Tên: %s) ---\n" +
                        "%s\n" +
                        "--- KẾT THÚC NỘI DUNG BÀI HỌC ---\n\n" +
                        "Câu hỏi của học viên: \"%s\"\n\n" +
                        "Hãy trả lời câu hỏi trên một cách ngắn gọn (tối đa 150 từ) và sư phạm. " +
                        "Nếu câu hỏi KHÔNG LIÊN QUAN đến nội dung, hãy lịch sự từ chối.",
                lesson.getName(),
                lessonContent,
                userQuestion
        );

        // 3. Gọi Helper API
        return callGeminiApi(prompt, "getContextualAnswer");
    }

    /**
     * MỚI (Cho MH-15 Nút Tóm tắt): Tóm tắt nội dung bài học
     */
    public String summarizeLesson(Long lessonId) {
        logger.info("AiLearningService: Bắt đầu tóm tắt lessonId: {}", lessonId);

        Lesson lesson;
        try {
            lesson = lessonService.getLessonById(lessonId.intValue());
        } catch (Exception e) {
            logger.error("❌ Lỗi summarizeLesson (không tìm thấy lesson): {}", e.getMessage());
            return "Lỗi: Không tìm thấy bài học để tóm tắt.";
        }

        String lessonContent = stripHtml(lesson.getHtmlContent());

        String prompt = String.format(
                "Bạn là một trợ lý AI. Hãy đọc nội dung sau và tóm tắt các ý chính quan trọng nhất " +
                        "thành một danh sách gạch đầu dòng (bullet points) ngắn gọn.\n" +
                        "Trả lời bằng Tiếng Việt.\n\n" +
                        "--- NỘI DUNG BÀI HỌC (Tên: %s) ---\n" +
                        "%s\n" +
                        "--- KẾT THÚC NỘI DUNG ---\n\n" +
                        "Tóm tắt của bạn:",
                lesson.getName(),
                lessonContent
        );

        return callGeminiApi(prompt, "summarizeLesson");
    }

    // --- CÁC TÍNH NĂNG CÓ SẴN CỦA BẠN (CHO QUIZ VÀ LAB) ---
    // (Giữ nguyên các phương thức này cho MH-17 và MH-19)

    /** 1. Lấy gợi ý cho Quiz (Giữ nguyên) */
    public String getHintForQuestion(String questionContent) {
        logger.info("AIService: Tạo gợi ý cho câu hỏi...");
        String prompt = "Hãy đưa ra một gợi ý nhỏ (dưới 20 từ) cho câu hỏi trắc nghiệm sau. "
                + "KHÔNG tiết lộ đáp án. Gợi ý bằng Tiếng Việt:\n\n" + questionContent;
        return callGeminiApi(prompt, "getHintForQuestion");
    }

    /** 2. Hỗ trợ Lab (Chat) (Giữ nguyên) */
    public String getLabHelp(String labContext, String userQuestion) {
        logger.info("AIService: Xử lý hỗ trợ Lab...");
        String prompt = "Bối cảnh: " + labContext + "\n\nCâu hỏi của học viên: " + userQuestion
                + "\n\nTrả lời hỗ trợ, không làm hộ, bằng Tiếng Việt:";
        return callGeminiApi(prompt, "getLabHelp");
    }

    /** 3. Chấm điểm Lab trả về JSON (Giữ nguyên) */
    public String gradeLab(String labCriteria, String userCode) {
        logger.info("AIService: Bắt đầu chấm điểm Lab...");
        String prompt = "Bạn là giám khảo. Chấm code sau:\n---CODE---\n"
                + userCode + "\n---END CODE---\nDựa trên tiêu chí: " + labCriteria
                + "\nChỉ trả về JSON theo schema {\"score\": number, \"feedback\": string}";

        Schema scoreSchema = Schema.builder().type("number").build();
        Schema feedbackSchema = Schema.builder().type("string").build();

        Schema jsonSchema = Schema.builder()
                .type("object")
                .properties(ImmutableMap.of(
                        "score", scoreSchema,
                        "feedback", feedbackSchema
                ))
                .required(ImmutableList.of("score", "feedback"))
                .build();

        GenerateContentConfig config = GenerateContentConfig.builder()
                .responseMimeType("application/json")
                .responseSchema(jsonSchema)
                .candidateCount(1)
                .build();

        try {
            GenerateContentResponse response = this.geminiClient.models
                    .generateContent("gemini-2.5-flash", prompt, config);
            String jsonResponse = response.text();
            return extractJson(jsonResponse);
        } catch (Exception e) {
            logger.error("❌ Lỗi gradeLab: {}", e.getMessage(), e);
            return "{\"score\":0,\"feedback\":\"Lỗi: AI trả về định dạng không hợp lệ.\"}";
        }
    }

    // --- HELPER METHODS (Giữ nguyên và Bổ sung) ---

    /** Hàm lọc JSON sạch (Giữ nguyên) */
    private String extractJson(String aiResponse) {
        Pattern pattern = Pattern.compile("```json\\s*(\\{.*\\})\\s*```|(\\{.*\\})", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(aiResponse);
        if (matcher.find()) {
            String json = (matcher.group(1) != null) ? matcher.group(1) : matcher.group(2);
            if (json != null) {
                logger.info("✅ Đã lọc được JSON sạch từ AI");
                return json;
            }
        }
        if (aiResponse.trim().startsWith("{") && aiResponse.trim().endsWith("}")) {
            logger.info("✅ AI đã trả về JSON chuẩn");
            return aiResponse.trim();
        }
        logger.warn("⚠ Không thể lọc JSON từ: {}", aiResponse);
        return "{\"score\":0,\"feedback\":\"Lỗi: AI trả về định dạng không hợp lệ.\"}";
    }

    /** Helper gọi Gemini API đơn giản (Sửa đổi để linh hoạt hơn) */
    private String callGeminiApi(String prompt, String methodName) {
        if (this.geminiClient == null) {
            logger.warn("AIService ({}): Client chưa khởi tạo. Trả về lỗi.", methodName);
            return "Lỗi: Dịch vụ AI chưa sẵn sàng.";
        }
        try {
            GenerateContentResponse response = this.geminiClient.models
                    .generateContent("gemini-2.5-flash", prompt, null);
            String aiText = response.text();
            if (aiText == null) {
                throw new RuntimeException("AI không trả về nội dung.");
            }
            // Làm sạch các ký tự markdown phổ biến
            aiText = aiText.replace("*", "").replace("`", "").trim();
            logger.info("AI ({}) trả về: {}", methodName, aiText.substring(0, Math.min(aiText.length(), 70)) + "...");
            return aiText;
        } catch (Exception e) {
            logger.error("❌ Lỗi khi gọi API Gemini ({}): {}", methodName, e.getMessage(), e);
            return "Lỗi: Không thể kết nối tới AI.";
        }
    }

    /** Helper mới: Làm sạch HTML */
    private String stripHtml(String html) {
        if (html == null) return "";
        // Regex đơn giản để loại bỏ thẻ HTML, giữ lại nội dung
        return html.replaceAll("<[^>]*>", "\n").replaceAll("\\s+", " ").trim();
    }

    /** Async gọi AI (Giữ nguyên) */
    public CompletableFuture<String> getLabHelpAsync(String labContext, String userQuestion) {
        return CompletableFuture.supplyAsync(() -> getLabHelp(labContext, userQuestion));
    }

    // (Chúng ta không cần hàm chatWithContext cũ nữa
    // vì đã có getContextualAnswer mạnh mẽ hơn)
}