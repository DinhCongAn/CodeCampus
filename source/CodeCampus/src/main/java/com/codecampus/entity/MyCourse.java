package com.codecampus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "my_courses")
@Getter
@Setter
public class MyCourse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // Trong DB là INT, nên dùng Integer

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    // SỬA 1: Đổi BigDecimal -> Double
    // Lý do: Để khớp với tính toán trong Service và tránh lỗi "incompatible types"
    @Column(name = "progress_percent")
    private Double progressPercent;

    // SỬA 2: Đổi Long lastLessonId -> Lesson lastLesson
    // Lý do: Để Controller có thể gọi myCourse.getLastLesson().getId()
    // Hibernate sẽ tự động ánh xạ cột "last_lesson_id" vào object này.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_lesson_id")
    private Lesson lastLesson;

    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;

    @Column(name = "status")
    private String status; // Thêm cột status ('in_progress', 'completed') cho đầy đủ
}