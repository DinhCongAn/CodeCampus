package com.codecampus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Formula;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name; // (title trong mô tả)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CourseCategory category; // (tag line trong mô tả)

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description; // (product description)

    private String status;

    @Column(name = "is_featured")
    private Boolean isFeatured;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(name = "thumbnail_url", columnDefinition = "NTEXT")
    private String thumbnailUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Formula("(SELECT count(*) FROM lessons l WHERE l.course_id = id)")
    private Integer numberOfLessons;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }
    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}