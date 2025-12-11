package com.codecampus.service;

import com.codecampus.dto.QuestionReviewDto;
import com.codecampus.dto.QuizReviewDto;
import com.codecampus.entity.Lesson; // <-- THÊM MỚI
import com.codecampus.entity.Question;
import com.codecampus.entity.Quiz;
import com.codecampus.repository.QuestionRepository;
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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class AiLearningService { // <-- Đổi tên thành AiLearningService

    private static final Logger logger = LoggerFactory.getLogger(AiLearningService.class);

    private Client geminiClient;

    @Value("${google.api.key}")
    private String apiKey;

    @Autowired // <-- THÊM MỚI
    private LessonService lessonService; // <-- Dịch vụ để lấy nội dung bài học
    @Autowired
    private QuizService quizService; // Tiêm QuizService

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuizAttemptService quizAttemptService;

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
                """
                Bạn là một trợ giảng AI thông minh và thân thiện.
        
                Nhiệm vụ của bạn:
                1) Ưu tiên trả lời dựa trên NỘI DUNG BÀI HỌC được cung cấp.
                2) Nếu không tìm thấy thông tin liên quan TRONG bài học:
                   → Hãy mở rộng bằng kiến thức tương tự (related knowledge) từ cùng chủ đề.
                   → Tuyệt đối không nói sai, không phóng đoán.
                3) Câu trả lời phải rõ ràng, dễ hiểu, tối đa 150 từ.
                4) Giữ phong cách sư phạm và giải thích hợp lý.
                5) Nếu câu hỏi quá xa chủ đề (ví dụ hỏi về nấu ăn trong bài lập trình):
                   → Lịch sự từ chối và gợi ý quay lại nội dung bài học.
        
                --- BẮT ĐẦU NỘI DUNG BÀI HỌC (Tên: %s) ---
                %s
                --- KẾT THÚC NỘI DUNG BÀI HỌC ---
        
                Câu hỏi của học viên: "%s"
        
                Hãy đưa ra câu trả lời phù hợp nhất theo 3 mức độ ưu tiên:
                1) Trích từ đúng nội dung bài học → nếu có.
                2) Suy luận từ các khái niệm liên quan → nếu phù hợp.
                3) Từ chối lịch sự → nếu câu hỏi không liên quan.
                """,
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

    // === THÊM PHƯƠNG THỨC MỚI CHO MH-16 ===



    /**
     * MỚI (Cho MH-16): Lấy gợi ý ôn tập nhanh trước Quiz
     */
    public String getQuizPreparationTips(Integer quizId) {
        logger.info("AiLearningService: Lấy gợi ý ôn tập cho quizId: {}", quizId);

        Quiz quiz;
        try {
            quiz = quizService.findQuizById(quizId);
        } catch (Exception e) {
            logger.error("❌ Lỗi getQuizPreparationTips (không tìm thấy quiz): {}", e.getMessage());
            return "Lỗi: Không tìm thấy thông tin quiz.";
        }

        // Lấy ngữ cảnh từ Tên/Mô tả của Quiz và Khóa học
        String quizName = quiz.getName();
        String quizDesc = stripHtml(quiz.getDescription()); // Dùng lại hàm stripHtml
        String courseName = quiz.getCourse().getName();

        // Xây dựng Prompt
        String prompt = String.format(
                "Bạn là một trợ giảng AI. Học viên sắp làm bài quiz có tên \"%s\" " +
                        "thuộc khóa học \"%s\".\n" +
                        "Mô tả của quiz: \"%s\".\n\n" +
                        "Hãy đưa ra 3-5 gạch đầu dòng (bullet points) về các khái niệm quan trọng nhất " +
                        "mà học viên nên ôn tập. Trả lời NGẮN GỌN, súc tích, bằng Tiếng Việt.",
                quizName,
                courseName,
                quizDesc
        );

        // Gọi Helper API (hàm callGeminiApi đã có)
        return callGeminiApi(prompt, "getQuizPreparationTips");
    }

    // === THÊM PHƯƠNG THỨC MỚI CHO MH-17 ===
    /**
     * MỚI (Cho MH-17): Lấy gợi ý AI cho một câu hỏi cụ thể
     */
    public String getQuizQuestionHint(Integer questionId) {
        logger.info("AiLearningService: Lấy gợi ý cho questionId: {}", questionId);

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi"));

        String questionContent = stripHtml(question.getContent());

        // Tạo danh sách các lựa chọn (chỉ lấy nội dung)
        String optionsText = question.getAnswerOptions().stream()
                .map(option -> "- " + stripHtml(option.getContent()))
                .collect(Collectors.joining("\n"));

        // Xây dựng Prompt
        String prompt = String.format(
                "Bạn là một trợ giảng AI. Hãy đưa ra một GỢI Ý NGẮN GỌN (dưới 25 từ) " +
                        "cho câu hỏi trắc nghiệm sau. \n" +
                        "TUYỆT ĐỐI KHÔNG được tiết lộ đáp án đúng. \n" +
                        "Chỉ đưa ra một câu hỏi dẫn dắt hoặc một định nghĩa liên quan.\n" +
                        "Trả lời bằng Tiếng Việt.\n\n" +
                        "--- CÂU HỎI ---\n" +
                        "%s\n\n" +
                        "--- CÁC LỰA CHỌN ---\n" +
                        "%s\n\n" +
                        "Gợi ý của bạn:",
                questionContent,
                optionsText
        );

        return callGeminiApi(prompt, "getQuizQuestionHint");
    }

    // === THÊM PHƯƠNG THỨC MỚI CHO MH-18 ===

    /**
     * SỬA LẠI (MH-18): Gộp 2 hàm AI thành 1 hàm duy nhất
     */
    public String getFullQuizAnalysis(Integer attemptId, Integer userId) {
        logger.info("AiLearningService: Bắt đầu phân tích TOÀN BỘ cho attemptId: {} / user {}", attemptId, userId);

        // 1. Lấy dữ liệu review
        QuizReviewDto review = quizAttemptService.getQuizReview(attemptId, userId);

        // 2. Lọc ra các câu sai
        List<QuestionReviewDto> incorrectQuestions = review.getQuestions().stream()
                .filter(q -> !q.isCorrect())
                .toList();

        if (incorrectQuestions.isEmpty()) {
            return "Tuyệt vời! Bạn đã trả lời đúng tất cả các câu hỏi. Hãy tiếp tục phát huy!";
        }

        // 3. Xây dựng Prompt (Câu lệnh) mới
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Bạn là một gia sư AI. Một học viên vừa làm bài quiz và sai ")
                .append(incorrectQuestions.size()).append("/")
                .append(review.getTotalQuestions()).append(" câu.\n\n");

        promptBuilder.append("--- CHI TIẾT CÁC CÂU SAI ---\n");

        // Thêm chi tiết từng câu sai vào prompt
        for (int i = 0; i < incorrectQuestions.size(); i++) {
            QuestionReviewDto q = incorrectQuestions.get(i);
            promptBuilder.append("\n[Câu sai " + (i+1) + "]\n");
            promptBuilder.append("Câu hỏi: ").append(stripHtml(q.getContent())).append("\n");
            promptBuilder.append("Họ chọn (SAI): ").append(stripHtml(q.getUserAnswerContent())).append("\n");
            promptBuilder.append("Đáp án đúng là: ").append(stripHtml(q.getCorrectAnswerContent())).append("\n");
        }

        promptBuilder.append("\n--- HẾT CHI TIẾT ---\n\n");
        promptBuilder.append("Nhiệm vụ của bạn (Trả lời bằng Tiếng Việt, dùng Markdown):\n");
        promptBuilder.append("1. **Nhận xét tổng quan:** Dựa vào các câu sai, tìm ra 1-2 chủ đề chung mà học viên bị yếu và đưa ra lời khuyên (dưới 100 từ).\n");
        promptBuilder.append("2. **Giải thích chi tiết:** Đi qua TỪNG CÂU SAI (ở trên) và giải thích ngắn gọn tại sao đáp án của học viên sai và tại sao đáp án đúng lại chính xác.\n");

        return callGeminiApi(promptBuilder.toString(), "getFullQuizAnalysis");
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

    /**
     * Prompt để sinh câu hỏi trắc nghiệm tự động
     */
    public String generateQuestionsPrompt(String courseName, String topic, String level, int count) {
        return String.format("""
        Đóng vai trò chuyên gia giáo dục môn "%s". Tạo %d câu hỏi trắc nghiệm chủ đề "%s", độ khó %s.
        
        QUAN TRỌNG:
        1. Trả về JSON thuần (Raw JSON), KHÔNG dùng Markdown (```json).
        2. Bắt buộc phải có đúng 1 đáp án đúng ("isCorrect": true).
        3. Các đáp án sai phải là ("isCorrect": false).
        
        Cấu trúc JSON bắt buộc:
        {
          "questions": [
            {
              "content": "Nội dung câu hỏi?",
              "explanation": "Giải thích...",
              "answers": [
                { "content": "Đáp án A", "isCorrect": false },
                { "content": "Đáp án B (Đúng)", "isCorrect": true },
                { "content": "Đáp án C", "isCorrect": false },
                { "content": "Đáp án D", "isCorrect": false }
              ]
            }
          ]
        }
        """, courseName, count, topic, level);
    }

    /**
     * Phương thức gọi AI để sinh câu hỏi và trả về chuỗi JSON thô
     * @param contextTopic: Chủ đề + Ngữ cảnh môn học
     * @param count: Số lượng câu
     * @param level: Độ khó
     */
    public String generateQuizJson(String contextTopic, int count, String level) {
        // Prompt cực kỳ chi tiết về định dạng JSON
        String prompt = String.format("""
            Bạn là chuyên gia tạo đề thi trắc nghiệm.
            Nhiệm vụ: Tạo %d câu hỏi trắc nghiệm về chủ đề: "%s". Độ khó: %s.
            
            YÊU CẦU BẮT BUỘC VỀ JSON OUTPUT:
            1. Chỉ trả về 1 chuỗi JSON duy nhất. Không Markdown, không ```json```.
            2. Cấu trúc JSON phải chính xác như sau (chú ý trường 'isCorrect'):
            
            {
              "questions": [
                {
                  "content": "Nội dung câu hỏi?",
                  "explanation": "Giải thích ngắn gọn",
                  "answers": [
                    { "content": "Đáp án sai 1", "isCorrect": false },
                    { "content": "Đáp án ĐÚNG", "isCorrect": true },
                    { "content": "Đáp án sai 2", "isCorrect": false },
                    { "content": "Đáp án sai 3", "isCorrect": false }
                  ]
                }
              ]
            }
            
            LƯU Ý QUAN TRỌNG:
            - Mỗi câu hỏi BẮT BUỘC phải có đúng 1 đáp án có "isCorrect": true.
            - Không được bỏ field "isCorrect".
            - Ngôn ngữ: Tiếng Việt.
            """, count, contextTopic, level);

        return callGeminiApi(prompt, "generateQuizJson");
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
    public String callGeminiApi(String prompt, String methodName) {
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

}