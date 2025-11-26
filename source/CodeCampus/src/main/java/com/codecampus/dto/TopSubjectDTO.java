package com.codecampus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopSubjectDTO {
    private String name;      // Tên môn
    private String category;  // Tên danh mục
    private Long orders;      // Số lượng đơn
    private BigDecimal revenue; // Doanh thu
}