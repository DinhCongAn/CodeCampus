package com.codecampus.dto;

import lombok.Data; // Hoặc Getter/Setter

@Data
public class GeneratedQuestionDTO {
    private String content;
    private String explanation;
    private String answerA;
    private String answerB;
    private String answerC;
    private String answerD;
    private String correctChar; // A, B, C, D

    // Các field ID để binding khi lưu
    private Integer courseId;
    private Integer quizId;
    private Integer lessonId;
    private Integer levelId;
}