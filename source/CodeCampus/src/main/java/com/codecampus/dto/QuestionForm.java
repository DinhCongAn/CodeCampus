package com.codecampus.dto;

import lombok.Data;
import java.util.List;

@Data
public class QuestionForm {
    private Integer id;
    private Integer courseId;
    private Integer quizId; // <-- Để gán vào bài kiểm tra
    private Integer levelId;
    private String content;
    private String explanation;
    private List<AnswerForm> answers;

    @Data
    public static class AnswerForm {
        private String content;
        private boolean isCorrect;
    }
}