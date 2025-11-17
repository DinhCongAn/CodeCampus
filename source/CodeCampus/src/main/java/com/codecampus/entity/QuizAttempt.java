package com.codecampus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime; // <-- Đảm bảo import thư viện này

@Entity
@Table(name = "quiz_attempts")
@Getter
@Setter
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    // === SỬA Ở ĐÂY ===
    // Đảm bảo cột này tồn tại và có 'nullable = false'
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    private BigDecimal score;

    // Đảm bảo cột này tồn tại
    private String status;

    private String result;

    @Column(name = "ai_hint_count", nullable = false)
    private int aiHintCount = 0;

    /**
     * THÊM KHỐI NÀY VÀO
     * Tự động gán giá trị trước khi lưu (INSERT)
     */
    @PrePersist
    protected void onCreate() {
        startTime = LocalDateTime.now();
        status = "in_progress";
    }
}