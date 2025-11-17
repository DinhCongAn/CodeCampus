package com.codecampus.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaveAnswerRequest {
    private Integer attemptId;
    private Integer questionId;
    private Integer answerId; // ID của AnswerOption
}