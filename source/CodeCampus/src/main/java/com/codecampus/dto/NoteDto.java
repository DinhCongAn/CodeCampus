package com.codecampus.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO (Data Transfer Object) cho Note.
 * Đây là một lớp POJO an toàn để trả về dưới dạng JSON.
 */
@Getter
@Setter
public class NoteDto {
    private Long id;
    private String note;
    private Long lessonId;
    private Integer userId;

    // (Bạn có thể thêm các trường khác nếu cần, ví dụ: ngày tạo)
}