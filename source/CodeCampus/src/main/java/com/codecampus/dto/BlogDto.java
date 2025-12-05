package com.codecampus.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;

@Data
public class BlogDto {
    private Integer id;
    private String title;
    private String brief;
    private String content;
    private String status;
    private Boolean isFeatured;
    private LocalDateTime publishedAt;
    private Integer blogCategoryId;

    // File
    private MultipartFile thumbnail;
    private String thumbnailUrl;

    // MỚI: Để check quyền ở Frontend
    private String authorEmail;
}