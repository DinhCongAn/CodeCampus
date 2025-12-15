package com.codecampus.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
@Getter @Setter
@Data
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Nội dung câu hỏi
    @Column(name = "content", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;

    // Giải thích đáp án (Hiện ra sau khi nộp bài)
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String explanation;

    // Link ảnh/video đính kèm (Lấy từ cột media_url trong DB)
    @Column(name = "media_url")
    private String mediaUrl;

    // Trạng thái: 'active', 'draft', 'hidden'
    @Column(length = 50)
    private String status;

    // ================= QUAN HỆ =================

    // 1. Thuộc về Môn học (Bắt buộc)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    // 2. Thuộc về Bài học (Tùy chọn - Có thể null nếu là câu hỏi chung của khóa)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    // 3. Độ khó (Dễ, Trung bình, Khó)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_level_id")
    private QuestionLevel questionLevel;

    // 4. Danh sách Đáp án (Quan trọng: Cascade ALL để lưu Question là lưu luôn Answers)
    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnswerOption> answerOptions;


    @ManyToMany(mappedBy = "questions", fetch = FetchType.LAZY)
    private List<Quiz> quizzes = new ArrayList<>();
}