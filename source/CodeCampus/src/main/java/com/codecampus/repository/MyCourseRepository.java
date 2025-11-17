package com.codecampus.repository;

import com.codecampus.entity.MyCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MyCourseRepository extends JpaRepository<MyCourse, Long> {
    Optional<MyCourse> findByUserIdAndCourseId(Integer userId, Integer courseId);
}