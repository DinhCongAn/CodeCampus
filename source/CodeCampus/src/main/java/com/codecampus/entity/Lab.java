package com.codecampus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "labs")
@Getter
@Setter
public class Lab {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    // ... (các trường khác nếu có) ...

    /**
     * Đây là định nghĩa gây ra lỗi.
     * Nó khai báo "Tôi (Lab) được map bởi thuộc tính 'lab' trong Lesson".
     */
    @OneToOne(mappedBy = "lab", fetch = FetchType.LAZY)
    private Lesson lesson;
}