// src/main/java/com/codecampus/util/GeminiApiDebugger.java
package com.codecampus.util;

import com.google.genai.Client;
import com.google.genai.Models;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class GeminiApiDebugger implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(GeminiApiDebugger.class);

    @Value("${google.api.key}")
    private String apiKey;

    @Override
    public void run(String... args) {
        logger.info("=== BẮT ĐẦU KIỂM TRA GEMINI API (SDK 1.27.0) ===");

        if (apiKey == null || apiKey.isEmpty()) {
            logger.warn("!!! CẢNH BÁO: 'google.api.key' chưa được cấu hình !!!");
            return;
        }

        try {
            // 1. Khởi tạo client
            HttpOptions httpOptions = HttpOptions.builder()
                    .apiVersion("v1")
                    .build();

            Client geminiClient = Client.builder()
                    .apiKey(apiKey)
                    .httpOptions(httpOptions)
                    .build();

            logger.info("Khởi tạo Client thành công.");

            // 2. Truy cập field models
            Models models = geminiClient.models;

            String prompt = "Xin chào Gemini! Hãy trả lời ngắn gọn: OK";

            // 3. Gọi API (Sửa lại model "gemini-pro" cho v1)
            GenerateContentResponse response = models.generateContent("gemini-2.5-flash", prompt, null);

            String aiText = response.text();

            if (aiText != null && !aiText.isEmpty()) {
                logger.info("✅ Kết nối thành công! AI trả lời: {}", aiText);
            } else {
                logger.error("❌ Kết nối thành công nhưng AI không trả về nội dung.");
            }

        } catch (Exception e) {
            logger.error("❌ Lỗi khi kết nối Gemini SDK: {}", e.getMessage(), e);
        }

        logger.info("=== KẾT THÚC KIỂM TRA GEMINI API ===");
    }
}
// (KHÔNG CÓ GÌ KHÁC BÊN DƯỚI DÒNG NÀY)