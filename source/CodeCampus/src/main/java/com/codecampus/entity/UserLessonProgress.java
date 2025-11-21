package com.codecampus.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_lesson_progress")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLessonProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "is_completed")
    private boolean isCompleted;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;
}