package com.codecampus.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;

@Entity
@Table(name = "quizzes")
public class Quiz {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Cours course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_type_id")
    private TestType testType;

    @Nationalized
    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_level_id")
    private QuestionLevel examLevel;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "pass_rate_percentage", precision = 5, scale = 2)
    private BigDecimal passRatePercentage;

    @Nationalized
    @Lob
    @Column(name = "description")
    private String description;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Cours getCourse() {
        return course;
    }

    public void setCourse(Cours course) {
        this.course = course;
    }

    public TestType getTestType() {
        return testType;
    }

    public void setTestType(TestType testType) {
        this.testType = testType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public QuestionLevel getExamLevel() {
        return examLevel;
    }

    public void setExamLevel(QuestionLevel examLevel) {
        this.examLevel = examLevel;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public BigDecimal getPassRatePercentage() {
        return passRatePercentage;
    }

    public void setPassRatePercentage(BigDecimal passRatePercentage) {
        this.passRatePercentage = passRatePercentage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}