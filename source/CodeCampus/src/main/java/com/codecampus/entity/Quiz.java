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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_type_id")
    private TestType testType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_level_id")
    private QuestionLevel examLevel;

    @Column(nullable = false)
    private String name;

    // === BỔ SUNG CÁC TRƯỜNG BỊ THIẾU ===

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "pass_rate_percentage")
    private BigDecimal passRatePercentage;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description; // <-- LỖI CỦA BẠN LÀ DO THIẾU TRƯỜNG NÀY

    // ==================================

    @OneToMany(mappedBy = "quiz", fetch = FetchType.LAZY)
    private List<Lesson> lessons;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "quiz_questions",
            joinColumns = @JoinColumn(name = "quiz_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    @OrderBy("id ASC")
    private List<Question> questions;
}