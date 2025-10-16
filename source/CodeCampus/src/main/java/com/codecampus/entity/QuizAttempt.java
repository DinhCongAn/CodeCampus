package com.codecampus.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @ColumnDefault("getdate()")
    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @ColumnDefault("NULL")
    @Column(name = "end_time")
    private Instant endTime;

    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;

    @Nationalized
    @ColumnDefault("'in_progress'")
    @Column(name = "status", length = 50)
    private String status;

    @Nationalized
    @Column(name = "result", length = 50)
    private String result;

    @ColumnDefault("0")
    @Column(name = "ai_hint_count", nullable = false)
    private Integer aiHintCount;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Integer getAiHintCount() {
        return aiHintCount;
    }

    public void setAiHintCount(Integer aiHintCount) {
        this.aiHintCount = aiHintCount;
    }

}