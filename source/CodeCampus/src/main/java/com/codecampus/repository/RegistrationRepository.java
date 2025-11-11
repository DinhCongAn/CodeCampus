// src/main/java/com/codecampus/repository/RegistrationRepository.java
package com.codecampus.repository;

import com.codecampus.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Integer> {

    /**
     * 1. Hỗ trợ hàm getRegistrationByOrderCode (Trang chờ)
     * (Lấy luôn User và Course để gửi mail không bị lỗi Lazy)
     */
    @Query("SELECT r FROM Registration r JOIN FETCH r.user JOIN FETCH r.course WHERE r.orderCode = :orderCode")
    Optional<Registration> findByOrderCode(@Param("orderCode") String orderCode);

    /**
     * 2. Hỗ trợ hàm hasUserRegistered (Trang chi tiết)
     */
    boolean existsByUserIdAndCourseId(Integer userId, Integer courseId);

    /**
     * 3. Hỗ trợ hàm getCoursesByUserId (Trang Khóa học của tôi)
     * (Lấy luôn User và Course để hiển thị ra HTML)
     */
    /**
     * Hỗ trợ hàm getCoursesByUserId (Trang Khóa học của tôi)
     * (BỔ SUNG: JOIN FETCH r.pricePackage p)
     */
    @Query("SELECT r FROM Registration r " +
            "JOIN FETCH r.user u " +
            "JOIN FETCH r.course c " +
            "JOIN FETCH r.pricePackage p " +
            "JOIN c.category cat " + // Join với category để lọc
            "WHERE u.id = :userId " + // Lọc theo User
            // Lọc theo keyword (nếu có)
            "AND (:keyword IS NULL OR c.name LIKE %:keyword%) " +
            // Lọc theo categoryId (nếu có)
            "AND (:categoryId IS NULL OR cat.id = :categoryId) " +
            "ORDER BY r.registrationTime DESC")
    List<Registration> findByUserIdWithDetails(
            @Param("userId") Integer userId,
            @Param("keyword") String keyword,
            @Param("categoryId") Integer categoryId
    );
    /**
     * 4. Hỗ trợ hàm getPendingRegistrations (Trang Admin)
     */
    List<Registration> findByStatus(String status);

    boolean existsByUserIdAndCourseIdAndStatus(Integer userId, Integer courseId, String status);

    /**
     * Tìm một đơn hàng PENDING (chờ duyệt)
     * của một user, cho một khóa học, VÀ cho một gói giá cụ thể.
     */
    boolean existsByUserIdAndCourseIdAndPricePackageIdAndStatus(
            Integer userId, Integer courseId, Integer pricePackageId, String status
    );
}