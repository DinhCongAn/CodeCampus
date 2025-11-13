package com.codecampus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "lab_ai_interactions")
@Getter @Setter @NoArgsConstructor
public class LabAiInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "attempt_id")
    private LabAttempt attempt;

    @Column(name = "user_prompt", columnDefinition = "NVARCHAR(MAX)")
    private String userPrompt;

    @Column(name = "ai_response", columnDefinition = "NVARCHAR(MAX)")
    private String aiResponse;

    private LocalDateTime timestamp;
}