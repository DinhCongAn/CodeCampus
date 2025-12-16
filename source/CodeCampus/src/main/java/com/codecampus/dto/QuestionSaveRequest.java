package com.codecampus.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class QuestionSaveRequest {

    // Thêm các trường khóa ngoại để Service biết phải lưu vào đâu
    @NotNull(message = "ID khóa học không được để trống")
    private Long courseId;

    private Long quizId; // Optional
    private Long lessonId; // Optional

    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    private String content;

    @NotBlank(message = "Giải thích không được để trống")
    private String explanation; // Thêm trường giải thích

    @Min(value = 1, message = "Cấp độ khó phải hợp lệ")
    @NotNull(message = "ID cấp độ khó không được để trống")
    private Long levelId; // Đổi thành Long để khớp với ID Entity (giả định)

    // Quan trọng: Phải có ít nhất 2 đáp án
    @NotEmpty(message = "Câu hỏi phải có ít nhất 2 đáp án")
    @Size(min = 2, max = 4, message = "Số lượng đáp án phải từ 2 đến 4")
    @Valid // Kích hoạt validation trong AnswerOptionSaveRequest
    private List<AnswerOptionSaveRequest> answerOptions;
}