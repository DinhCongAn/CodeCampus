package com.codecampus.repository;
import com.codecampus.entity.QuestionLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionLevelRepository extends JpaRepository<QuestionLevel, Integer> {
    Optional<QuestionLevel> findByName(String name);
}