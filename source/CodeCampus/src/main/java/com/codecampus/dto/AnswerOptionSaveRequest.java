package com.codecampus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnswerOptionSaveRequest {

    @NotBlank(message = "Nội dung đáp án không được để trống")
    private String content;

    @NotNull(message = "Đáp án phải xác định là đúng hay sai")
    private Boolean isCorrect; // Phải dùng Boolean (Object) để kiểm tra null
}