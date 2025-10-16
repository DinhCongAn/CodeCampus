package com.codecampus.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "question_group")
public class QuestionGroup {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Nationalized
    @Column(name = "name")
    private String name;

    @Column(name = "questions_number")
    private Integer questionsNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_setting_id", nullable = false)
    private QuizSetting quizSetting;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getQuestionsNumber() {
        return questionsNumber;
    }

    public void setQuestionsNumber(Integer questionsNumber) {
        this.questionsNumber = questionsNumber;
    }

    public QuizSetting getQuizSetting() {
        return quizSetting;
    }

    public void setQuizSetting(QuizSetting quizSetting) {
        this.quizSetting = quizSetting;
    }

}