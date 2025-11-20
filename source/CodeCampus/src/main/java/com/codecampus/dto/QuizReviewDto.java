package com.codecampus.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class QuizReviewDto {
    private Integer attemptId;
    private String quizName;
    private BigDecimal score; // Điểm số
    private String result; // "Pass" or "Fail"
    private int totalQuestions;
    private int correctCount;
    private List<QuestionReviewDto> questions; // Danh sách các câu hỏi để review

    // (Constructor, Getter, Setter)

    private Integer quizId;
    private Integer lessonId;
}