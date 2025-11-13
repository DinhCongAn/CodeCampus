package com.codecampus.dto.gemini;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class GeminiContent {
    private List<GeminiPart> parts;
}
