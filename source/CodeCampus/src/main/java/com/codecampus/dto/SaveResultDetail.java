package com.codecampus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// DTO này dùng chung cho cả import Excel và lưu từng câu
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveResultDetail {
    private int totalRecords;
    private int successCount;
    private int failCount;
    private List<FailEntry> failDetails; // Danh sách các lỗi

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailEntry {
        private String sourceData; // Dữ liệu câu hỏi bị lỗi (VD: content)
        private String errorMessage; // Chi tiết lỗi (VD: Nội dung không được để trống)
    }
}