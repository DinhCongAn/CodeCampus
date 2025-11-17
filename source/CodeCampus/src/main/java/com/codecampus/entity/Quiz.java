package com.codecampus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List; // Phải là List (hoặc Set)

@Entity
@Table(name = "quizzes")
@Getter @Setter @NoArgsConstructor
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // (...các trường course, testType, examLevel, name, v.v...)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_type_id")
    private TestType testType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_level_id")
    private QuestionLevel examLevel;

    private String name;
    // ...

    // === SỬA Ở ĐÂY ===
    /**
     * ĐÂY LÀ BÊN KHÔNG SỞ HỮU (Inverse Side) - Quan hệ OneToMany.
     * 'mappedBy = "quiz"' trỏ đến thuộc tính 'private Quiz quiz' trong Lesson.java.
     * Một Quiz có thể nằm trong NHIỀU Lesson.
     */
    @OneToMany(mappedBy = "quiz", fetch = FetchType.LAZY)
    private List<Lesson> lessons; // Thay thế cho private Lesson lesson;

    // (Quan hệ ManyToMany với Question giữ nguyên)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "quiz_questions",
            joinColumns = @JoinColumn(name = "quiz_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    @OrderBy("id ASC")
    private List<Question> questions;
}