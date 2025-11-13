
package com.codecampus.repository;

import com.codecampus.entity.AnswerOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswerOptionRepository extends JpaRepository<AnswerOption, Integer> {
    // Không cần phương thức custom
}