package com.codecampus.repository;

import com.codecampus.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository <Question, Integer>{
}
