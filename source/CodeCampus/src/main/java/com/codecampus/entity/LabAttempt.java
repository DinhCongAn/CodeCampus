package com.codecampus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lab_attempts")
@Getter @Setter @NoArgsConstructor
public class LabAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "lab_id")
    private Lab lab;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String status; // 'in_progress', 'grading', 'graded'

    @Column(name = "submitted_content", columnDefinition = "NVARCHAR(MAX)")
    private String submittedContent; // Code của user

    @Column(name = "ai_grade")
    private BigDecimal aiGrade;

    @Column(name = "ai_feedback", columnDefinition = "NVARCHAR(MAX)")
    private String aiFeedback;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}