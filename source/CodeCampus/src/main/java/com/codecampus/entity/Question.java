package com.codecampus.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "questions")
public class Question {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_level_id")
    private QuestionLevel questionLevel;

    @Nationalized
    @ColumnDefault("'draft'")
    @Column(name = "status", length = 50)
    private String status;

    @Nationalized
    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Nationalized
    @Column(name = "media_url")
    private String mediaUrl;

    @Nationalized
    @Lob
    @Column(name = "explanation")
    private String explanation;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public void setLesson(Lesson lesson) {
        this.lesson = lesson;
    }

    public QuestionLevel getQuestionLevel() {
        return questionLevel;
    }

    public void setQuestionLevel(QuestionLevel questionLevel) {
        this.questionLevel = questionLevel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

}