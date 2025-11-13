package com.codecampus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lessons")
@Getter @Setter @NoArgsConstructor
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne
    @JoinColumn(name = "lesson_type_id")
    private LessonType lessonType;

    private String name;
    private String topic;

    @Column(name = "order_number")
    private Integer orderNumber;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "html_content", columnDefinition = "NVARCHAR(MAX)")
    private String htmlContent;

    // 1 Bài học có thể là 1 Quiz
    @OneToOne
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    // 1 Bài học có thể là 1 Lab (MỚI)
    @OneToOne
    @JoinColumn(name = "lab_id")
    private Lab lab;

    private String status;

    // (Trường package_id trong DB có vẻ dùng để giới hạn bài học theo gói)
    @Column(name = "package_id")
    private Integer packageId;
}