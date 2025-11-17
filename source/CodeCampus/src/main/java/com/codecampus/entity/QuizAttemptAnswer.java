package com.codecampus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "quiz_attempt_answers")
@Getter
@Setter
public class QuizAttemptAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id")
    private QuizAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_answer_option_id")
    private AnswerOption selectedAnswerOption;

    // (Bạn có thể thêm 2 cột này vào CSDL để review dễ hơn)
    // @Column(name = "is_correct")
    // private boolean isCorrect;

    // @Column(name = "time_taken_seconds")
    // private Integer timeTakenSeconds;
}