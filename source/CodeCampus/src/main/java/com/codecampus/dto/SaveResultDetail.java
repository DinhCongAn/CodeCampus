package com.codecampus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveResultDetail {
    private int totalRecords;
    private int successCount;
    private int failCount;

    private List<FailEntry> failDetails;
    private List<SuccessEntry> successDetails; // Bổ sung cho báo cáo thành công

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailEntry {
        private String sourceData;
        private String errorMessage;
        private QuestionSaveRequest originalRequest; // Dữ liệu gốc DTO bị lỗi
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuccessEntry {
        private QuestionSaveRequest originalRequest; // Dữ liệu gốc DTO đã lưu
        private String message;
    }
}