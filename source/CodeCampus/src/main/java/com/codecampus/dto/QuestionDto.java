package com.codecampus.dto;

import com.codecampus.entity.Question;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class QuestionDto {
    private Integer id;
    private String content;
    private List<AnswerOptionDto> answerOptions;

    // Hàm helper để chuyển đổi Entity sang DTO
    public static QuestionDto fromEntity(Question entity) {
        QuestionDto dto = new QuestionDto();
        dto.setId(entity.getId());
        dto.setContent(entity.getContent());

        // Quan trọng: Chuyển đổi danh sách con (AnswerOption) sang DTO
        if (entity.getAnswerOptions() != null) {
            dto.setAnswerOptions(
                    entity.getAnswerOptions().stream()
                            .map(AnswerOptionDto::fromEntity) // Gọi hàm DTO lồng nhau
                            .collect(Collectors.toList())
            );
        } else {
            dto.setAnswerOptions(Collections.emptyList());
        }
        return dto;
    }
}