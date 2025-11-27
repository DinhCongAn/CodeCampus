package com.codecampus.repository;
import com.codecampus.entity.QuestionLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionLevelRepository extends JpaRepository<QuestionLevel, Integer> {

}