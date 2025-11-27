package com.codecampus.repository;

import com.codecampus.entity.LessonType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonTypeRepository extends JpaRepository<LessonType, Integer> {

    // Tìm theo tên (nếu cần check logic sau này)
    LessonType findByName(String name);
}