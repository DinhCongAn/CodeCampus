package com.codecampus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "quizzes")
@Getter @Setter @NoArgsConstructor
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne
    @JoinColumn(name = "test_type_id")
    private TestType testType;

    @ManyToOne
    @JoinColumn(name = "exam_level_id")
    private QuestionLevel examLevel; // (DB dùng exam_level_id)

    @OneToOne(mappedBy = "quiz") // 1-1 ngược lại Lesson
    private Lesson lesson;

    private String name;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "pass_rate_percentage")
    private BigDecimal passRatePercentage;

    private String description;

    // Quan hệ Nhiều-Nhiều với Question (qua bảng quiz_questions)
    @ManyToMany
    @JoinTable(
            name = "quiz_questions",
            joinColumns = @JoinColumn(name = "quiz_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    @OrderBy("id ASC") // Sắp xếp câu hỏi theo ID
    private List<Question> questions;
}