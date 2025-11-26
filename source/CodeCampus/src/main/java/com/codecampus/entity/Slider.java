package com.codecampus.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sliders")
@Getter
@Setter
public class Slider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "title")
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    // [QUAN TRỌNG] XÓA DÒNG @NotBlank Ở ĐÂY ĐI
    // Vì nếu upload file thì trường này ban đầu sẽ null,
    // Controller sẽ tự điền sau khi upload xong.
    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "link_url")
    private String backlink;

    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String notes;

    @Column(name = "status")
    private String status;

    @Column(name = "order_number")
    private Integer orderNumber = 0;
}