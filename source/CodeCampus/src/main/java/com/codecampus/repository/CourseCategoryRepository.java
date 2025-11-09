package com.codecampus.repository;

import com.codecampus.entity.CourseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseCategoryRepository extends JpaRepository<CourseCategory, Integer> {
    // Lấy các danh mục đang hoạt động cho sidebar
    List<CourseCategory> findByIsActive(boolean isActive);
}