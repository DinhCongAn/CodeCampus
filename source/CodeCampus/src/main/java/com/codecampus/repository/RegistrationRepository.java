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
    @Query("SELECT r FROM Registration r JOIN FETCH r.user u JOIN FETCH r.course c " +
            "WHERE u.id = :userId ORDER BY r.registrationTime DESC")
    List<Registration> findByUserIdWithDetails(@Param("userId") Integer userId);

    /**
     * 4. Hỗ trợ hàm getPendingRegistrations (Trang Admin)
     */
    List<Registration> findByStatus(String status);


}