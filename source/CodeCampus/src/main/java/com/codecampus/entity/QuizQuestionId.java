package com.codecampus.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class QuizQuestionId implements Serializable {
    private static final long serialVersionUID = -8255021447893752331L;
    @Column(name = "quiz_id", nullable = false)
    private Integer quizId;

    @Column(name = "question_id", nullable = false)
    private Integer questionId;

    public Integer getQuizId() {
        return quizId;
    }

    public void setQuizId(Integer quizId) {
        this.quizId = quizId;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        QuizQuestionId entity = (QuizQuestionId) o;
        return Objects.equals(this.questionId, entity.questionId) &&
                Objects.equals(this.quizId, entity.quizId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(questionId, quizId);
    }

}