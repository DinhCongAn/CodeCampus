package com.codecampus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "labs")
@Getter @Setter @NoArgsConstructor
public class Lab {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(mappedBy = "lab") // 1-1 ngược lại Lesson
    private Lesson lesson;

    private String name;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "lab_type")
    private String labType;

    @Column(name = "evaluation_criteria", columnDefinition = "NVARCHAR(MAX)")
    private String evaluationCriteria; // Tiêu chí cho AI chấm

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}