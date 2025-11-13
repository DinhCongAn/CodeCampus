package com.codecampus.dto.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // Bỏ qua các trường không cần
public class GeminiResponse {
    private List<GeminiCandidate> candidates;

    // Helper (Hàm tiện ích) để lấy text an toàn
    public String getFirstTextResponse() {
        try {
            return candidates.get(0).getContent().getParts().get(0).getText();
        } catch (Exception e) {
            return null; // Hoặc văng lỗi
        }
    }
}
