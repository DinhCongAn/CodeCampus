package com.codecampus.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;

@Data
public class BlogDto {
    private Integer id;
    private String title;
    private String brief; // Map với summary
    private String content;
    private String status;
    private Boolean isFeatured;
    private LocalDateTime publishedAt;
    private Integer blogCategoryId;

    // Xử lý ảnh
    private MultipartFile thumbnail; // File mới upload
    private String thumbnailUrl;     // Đường dẫn ảnh cũ
}