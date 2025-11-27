package com.codecampus.repository;

import com.codecampus.entity.PricePackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PricePackageRepository extends JpaRepository<PricePackage, Integer> {

    // Tìm gói giá thấp nhất (ưu tiên sale_price) của một khóa học
    @Query(value = "SELECT TOP 1 * FROM price_packages " +
            "WHERE course_id = ?1 AND status = 'active' " +
            "ORDER BY COALESCE(sale_price, list_price) ASC",
            nativeQuery = true)
    Optional<PricePackage> findLowestPricePackageByCourseId(Integer courseId);
    List<PricePackage> findByCourseId(Integer courseId);
    List<PricePackage> getPackagesByCourseId(Integer courseId);
    List<PricePackage> findByCourseId(Long courseId); // Lưu ý CourseId là Long hay Integer thì sửa cho khớp

    @Query("SELECT COUNT(p) > 0 FROM PricePackage p " +
            "WHERE p.course.id = :courseId " +
            "AND LOWER(p.name) = LOWER(:name) " +
            "AND (:id IS NULL OR p.id != :id)")
    boolean existsByNameAndCourse(@Param("courseId") Integer courseId,
                                  @Param("name") String name,
                                  @Param("id") Integer id);
}