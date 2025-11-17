package com.codecampus.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionReviewDto {
    private Integer questionId;
    private String content; // Nội dung câu hỏi
    private String explanation; // Giải thích có sẵn (nếu có)
    private String userAnswerContent; // Nội dung câu trả lời của user
    private String correctAnswerContent; // Nội dung đáp án đúng
    private boolean isCorrect; // User trả lời đúng hay sai

    // (Constructor, Getter, Setter)
}