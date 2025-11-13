package com.codecampus.dto.gemini;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class GeminiRequest {
    private List<GeminiContent> contents;
}
