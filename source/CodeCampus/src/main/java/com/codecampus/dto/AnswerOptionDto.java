package com.codecampus.dto;

import com.codecampus.entity.AnswerOption;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerOptionDto {
    private Integer id;
    private String content;

    // Hàm helper để chuyển đổi Entity sang DTO
    public static AnswerOptionDto fromEntity(AnswerOption entity) {
        AnswerOptionDto dto = new AnswerOptionDto();
        dto.setId(entity.getId());
        dto.setContent(entity.getContent());
        return dto;
    }
}