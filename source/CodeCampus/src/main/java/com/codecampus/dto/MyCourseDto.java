package com.codecampus.dto;

import com.codecampus.entity.Registration;
import lombok.Data;

@Data
public class MyCourseDto {
    private Registration registration;
    private Double progressPercent;

    // SỬA TẠI ĐÂY: Đổi Integer thành Long
    private Long lastLessonId;

    // SỬA TẠI ĐÂY: Cập nhật Constructor nhận Long
    public MyCourseDto(Registration registration, Double progressPercent, Long lastLessonId) {
        this.registration = registration;
        this.progressPercent = progressPercent != null ? progressPercent : 0.0;
        this.lastLessonId = lastLessonId;
    }
}