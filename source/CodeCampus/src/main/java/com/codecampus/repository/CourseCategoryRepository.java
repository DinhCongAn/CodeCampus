package com.codecampus.repository;

import com.codecampus.entity.CourseCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseCategoryRepository extends JpaRepository<CourseCategory, Integer> {
    // Lấy các danh mục đang hoạt động cho sidebar
    List<CourseCategory> findByIsActive(boolean isActive);
    // 1. Tìm theo tên chính xác (Để check trùng)
    @Query("SELECT c FROM CourseCategory c WHERE LOWER(c.name) = LOWER(:name)")
    CourseCategory findByNameIgnoreCase(@Param("name") String name);

    // 2. Tìm kiếm Admin (Native Query cho SQL Server)
    // isActive: true/false. Nếu status = null thì lấy tất cả
    @Query(value = "SELECT * FROM course_categories c " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR " +
            "c.name COLLATE Latin1_General_CI_AI LIKE CONCAT('%', :keyword, '%')) " +
            "AND (:isActive IS NULL OR c.is_active = :isActive) " +
            "ORDER BY c.name ASC",
            countQuery = "SELECT COUNT(*) FROM course_categories c " +
                    "WHERE (:keyword IS NULL OR :keyword = '' OR " +
                    "c.name COLLATE Latin1_General_CI_AI LIKE CONCAT('%', :keyword, '%')) " +
                    "AND (:isActive IS NULL OR c.is_active = :isActive)",
            nativeQuery = true)
    Page<CourseCategory> findCategoriesAdmin(@Param("keyword") String keyword,
                                             @Param("isActive") Boolean isActive,
                                             Pageable pageable);
}