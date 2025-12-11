package com.codecampus.repository;

import com.codecampus.entity.MyCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MyCourseRepository extends JpaRepository<MyCourse, Integer> {

    // Tìm thông tin tiến độ tổng quan của User trong 1 Course
    MyCourse findByUserIdAndCourseId(Integer userId, Integer courseId);
    List<MyCourse> findByUserId(Integer userId);
}