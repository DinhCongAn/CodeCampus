package com.codecampus.service;

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
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.CompletableFuture;

@Service
public class AIService {

    private static final Logger logger = LoggerFactory.getLogger(AIService.class);

    private Client geminiClient;

    @Value("${google.api.key}")
    private String apiKey;

    /** Khởi tạo Gemini Client (API v1beta, hỗ trợ models mới hơn) */
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
            logger.info("✅ AIService: Gemini Client khởi tạo thành công với API v1beta");
        } catch (Exception e) {
            logger.error("❌ Lỗi khởi tạo Gemini Client: {}", e.getMessage(), e);
        }
    }

    /** 1. Lấy gợi ý cho Quiz */
    public String getHintForQuestion(String questionContent) {
        logger.info("AIService: Tạo gợi ý cho câu hỏi...");
        String prompt = "Hãy đưa ra một gợi ý nhỏ (dưới 20 từ) cho câu hỏi trắc nghiệm sau. "
                + "KHÔNG tiết lộ đáp án. Gợi ý bằng Tiếng Việt:\n\n" + questionContent;
        try {
            GenerateContentResponse response = this.geminiClient.models
                    .generateContent("gemini-2.5-flash", prompt, null);
            return response.text().replace("*", "").replace("`", "");
        } catch (Exception e) {
            logger.error("❌ Lỗi getHintForQuestion: {}", e.getMessage(), e);
            return "Lỗi: Không thể lấy gợi ý từ AI.";
        }
    }

    /** 2. Hỗ trợ Lab (Chat) */
    public String getLabHelp(String labContext, String userQuestion) {
        logger.info("AIService: Xử lý hỗ trợ Lab...");
        String prompt = "Bối cảnh: " + labContext + "\n\nCâu hỏi của học viên: " + userQuestion
                + "\n\nTrả lời hỗ trợ, không làm hộ, bằng Tiếng Việt:";
        try {
            GenerateContentResponse response = this.geminiClient.models
                    .generateContent("gemini-2.5-flash", prompt, null);
            return response.text();
        } catch (Exception e) {
            logger.error("❌ Lỗi getLabHelp: {}", e.getMessage(), e);
            return "Lỗi: Không thể kết nối với AI hỗ trợ.";
        }
    }

    /** 3. Chấm điểm Lab trả về JSON {score, feedback} */
    public String gradeLab(String labCriteria, String userCode) {
        logger.info("AIService: Bắt đầu chấm điểm Lab...");
        String prompt = "Bạn là giám khảo. Chấm code sau:\n---CODE---\n"
                + userCode + "\n---END CODE---\nDựa trên tiêu chí: " + labCriteria
                + "\nChỉ trả về JSON theo schema {\"score\": number, \"feedback\": string}";

        // ==== SỬA LỖI SCHEMA ====
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
        // ==== KẾT THÚC SCHEMA ====

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

    /** Hàm lọc JSON sạch từ phản hồi AI */
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

    /** Helper gọi Gemini API đơn giản (dùng cho GetHint & GetLabHelp) */
    private String callGeminiApi(String prompt) {
        try {
            GenerateContentResponse response = this.geminiClient.models
                    .generateContent("gemini-2.5-flash", prompt, null);
            String aiText = (response != null) ? response.text() : null;
            if (aiText == null) {
                throw new RuntimeException("AI không trả về nội dung.");
            }
            logger.info("AI trả về (50 ký tự đầu): {}", aiText.substring(0, Math.min(aiText.length(), 50)) + "...");
            return aiText;
        } catch (Exception e) {
            logger.error("❌ Lỗi khi gọi API Gemini: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể kết nối tới AI.", e);
        }
    }

    /** Async gọi AI (ví dụ dùng cho LabHelp) */
    public CompletableFuture<String> getLabHelpAsync(String labContext, String userQuestion) {
        return CompletableFuture.supplyAsync(() -> getLabHelp(labContext, userQuestion));
    }

    /** 4. Chat hỗ trợ học tập - xử lý câu hỏi với ngữ cảnh */
    public String chatWithContext(String userMessage, String context, String contextName) {
        logger.info("AIService: Xử lý chat với ngữ cảnh - context: {}, contextName: {}", context, contextName);

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Bạn là trợ lý AI hỗ trợ học tập. QUAN TRỌNG: Trả lời NGẮN GỌN (tối đa 150 từ), KHÔNG viết code CSS/HTML dài, chỉ giải thích ý chính.\n\n");

        // Thêm ngữ cảnh nếu có
        if (context != null && !context.isEmpty() && contextName != null && !contextName.isEmpty()) {
            promptBuilder.append("Ngữ cảnh: Học viên đang học về '").append(contextName).append("'.\n");
            if ("lesson".equals(context)) {
                promptBuilder.append("Đây là bài học. Giải thích ngắn gọn, dễ hiểu.\n\n");
            } else if ("lab".equals(context)) {
                promptBuilder.append("Đây là lab thực hành. Hướng dẫn ngắn gọn, không làm hộ.\n\n");
            } else if ("quiz".equals(context)) {
                promptBuilder.append("Đây là quiz. Gợi ý ngắn gọn, KHÔNG tiết lộ đáp án.\n\n");
            }
        }

        promptBuilder.append("Câu hỏi: ").append(userMessage).append("\n\n");
        promptBuilder.append("Trả lời NGẮN GỌN (tối đa 150 từ), bằng Tiếng Việt, KHÔNG viết code CSS/HTML dài. Chỉ giải thích ý chính và ví dụ đơn giản.");

        try {
            GenerateContentResponse response = this.geminiClient.models
                    .generateContent("gemini-2.5-flash", promptBuilder.toString(), null);
            String answer = response.text();
            // Làm sạch format - loại bỏ code blocks và CSS dài
            answer = answer.replace("*", "").replace("```", "").trim();
            // Loại bỏ các đoạn code CSS/HTML dài (nếu có)
            answer = answer.replaceAll("(?s)<style[^>]*>.*?</style>", "");
            answer = answer.replaceAll("(?s)<code[^>]*>.*?</code>", "");
            // Giới hạn độ dài nếu quá dài
            if (answer.length() > 500) {
                answer = answer.substring(0, 500) + "...";
            }
            return answer.trim();
        } catch (Exception e) {
            logger.error("❌ Lỗi chatWithContext: {}", e.getMessage(), e);
            return "Xin lỗi, tôi không thể kết nối với AI lúc này. Vui lòng thử lại sau.";
        }
    }
}
