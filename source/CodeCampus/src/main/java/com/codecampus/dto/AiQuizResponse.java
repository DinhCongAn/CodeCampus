package com.codecampus.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiQuizResponse {
    private List<AiQuestion> questions;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiQuestion {
        private String content;
        private String explanation;
        private List<AiAnswer> answers;
    }

//    @Data
//    @JsonIgnoreProperties(ignoreUnknown = true)
//    public static class AiAnswer {
//        private String content;
//        private boolean isCorrect;
//    }
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiAnswer {
        private String content;

        // Thêm annotation này để chắc chắn map đúng trường JSON
        @JsonProperty("isCorrect")
        private boolean isCorrect;
}}