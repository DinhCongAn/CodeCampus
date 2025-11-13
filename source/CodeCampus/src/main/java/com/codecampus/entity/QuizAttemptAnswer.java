package com.codecampus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "quiz_attempt_answers")
@Getter @Setter @NoArgsConstructor
public class QuizAttemptAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "attempt_id")
    private QuizAttempt attempt;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    // Đáp án user đã chọn
    @ManyToOne
    @JoinColumn(name = "selected_answer_option_id")
    private AnswerOption selectedAnswerOption;

    @Column(name = "is_correct")
    private boolean isCorrect;

    // (Các trường time_taken_seconds, marked_for_review nếu cần)
}