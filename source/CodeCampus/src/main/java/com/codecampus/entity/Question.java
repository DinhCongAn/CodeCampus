package com.codecampus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Entity
@Table(name = "questions")
@Getter @Setter @NoArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "content", columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(name = "explanation", columnDefinition = "NVARCHAR(MAX)")
    private String explanation; // Giải thích (dùng cho Review)

    @Column(name = "status")
    private String status;

    @Column(name = "media_url")
    private String mediaUrl;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @ManyToOne
    @JoinColumn(name = "question_level_id")
    private QuestionLevel questionLevel;

    // 1-Nhiều với các lựa chọn (Lấy luôn đáp án)
    @OneToMany(mappedBy = "question", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @OrderBy("orderNumber ASC")
    private List<AnswerOption> answerOptions;

    // Nhiều-Nhiều ngược lại Quiz
    @ManyToMany(mappedBy = "questions")
    private List<Quiz> quizzes;

    // 1-Nhiều với Media (bảng question_media)
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    @OrderBy("orderNumber ASC")
    private List<QuestionMedia> questionMediaList;
}