// src/main/java/com/codecampus/repository/RegistrationRepository.java
package com.codecampus.repository;

import com.codecampus.dto.TopSubjectDTO;
import com.codecampus.entity.Registration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    // --- QUAN TRỌNG ĐỂ CHECK QUYỀN ---
    // Tìm xem user đã mua khóa học này chưa
    Registration findByUserIdAndCourseId(Integer userId, Integer courseId);

    // Các hàm search cũ phục vụ cho trang My Courses (đã có trong logic cũ của bạn)
    // Ví dụ logic search:
    @Query("SELECT r FROM Registration r WHERE r.user.id = :userId " +
            "AND (:keyword IS NULL OR r.course.name LIKE %:keyword%) " +
            "AND (:categoryId IS NULL OR r.course.category.id = :categoryId)")
    List<Registration> findByUserIdAndFilters(@Param("userId") Integer userId,
                                              @Param("keyword") String keyword,
                                              @Param("categoryId") Integer categoryId);

    // 1. Tính tổng doanh thu theo khoảng thời gian (Chỉ đơn COMPLETED)
    @Query("SELECT COALESCE(SUM(r.totalCost), 0) FROM Registration r " +
            "WHERE r.status = 'COMPLETED' AND r.registrationTime BETWEEN :start AND :end")
    BigDecimal sumRevenue(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 2. Đếm số đơn theo trạng thái
    @Query("SELECT COUNT(r) FROM Registration r " +
            "WHERE r.status = :status AND r.registrationTime BETWEEN :start AND :end")
    Long countByStatus(@Param("status") String status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 3. Đếm TẤT CẢ đơn trong khoảng thời gian (Submitted)
    @Query("SELECT COUNT(r) FROM Registration r WHERE r.registrationTime BETWEEN :start AND :end")
    Long countAllInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 4. Lấy dữ liệu biểu đồ xu hướng (Group by Date)
    // Trả về: [Ngày (java.sql.Date), Số lượng]
    @Query("SELECT CAST(r.registrationTime AS date), COUNT(r) FROM Registration r " +
            "WHERE r.registrationTime BETWEEN :start AND :end " +
            "GROUP BY CAST(r.registrationTime AS date) ORDER BY CAST(r.registrationTime AS date)")
    List<Object[]> countOrdersByDay(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT CAST(r.registrationTime AS date), COUNT(r) FROM Registration r " +
            "WHERE r.status = 'COMPLETED' AND r.registrationTime BETWEEN :start AND :end " +
            "GROUP BY CAST(r.registrationTime AS date) ORDER BY CAST(r.registrationTime AS date)")
    List<Object[]> countSuccessOrdersByDay(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 5. Doanh thu theo danh mục
    @Query("SELECT c.category.name, SUM(r.totalCost) FROM Registration r " +
            "JOIN r.course c " +
            "WHERE r.status = 'COMPLETED' AND r.registrationTime BETWEEN :start AND :end " +
            "GROUP BY c.category.name")
    List<Object[]> getRevenueByCategory(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 6. Top 5 khóa học bán chạy
    @Query("SELECT new com.codecampus.dto.TopSubjectDTO(c.name, c.category.name, COUNT(r), SUM(r.totalCost)) " +
            "FROM Registration r JOIN r.course c " +
            "WHERE r.status = 'COMPLETED' AND r.registrationTime BETWEEN :start AND :end " +
            "GROUP BY c.id, c.name, c.category.name " +
            "ORDER BY SUM(r.totalCost) DESC")
    List<TopSubjectDTO> getTopSubjects(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    // 7. Đếm số khách hàng MỚI MUA LẦN ĐẦU trong kỳ (Logic: User có đơn completed đầu tiên nằm trong khoảng này)
    @Query("SELECT COUNT(DISTINCT r.user) FROM Registration r WHERE r.status = 'COMPLETED' " +
            "AND r.registrationTime BETWEEN :start AND :end " +
            "AND r.user.id NOT IN (SELECT r2.user.id FROM Registration r2 WHERE r2.status = 'COMPLETED' AND r2.registrationTime < :start)")
    Long countNewPayingCustomers(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Tìm kiếm đơn hàng (Bất chấp Hoa/Thường + Có dấu/Không dấu)
    @Query(value = "SELECT r.* FROM registrations r " +
            "LEFT JOIN users u ON r.user_id = u.id " +
            "LEFT JOIN courses c ON r.course_id = c.id " +
            "WHERE (:status IS NULL OR r.status = :status) " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "r.order_code COLLATE Latin1_General_CI_AI LIKE CONCAT('%', :keyword, '%') OR " +
            "u.email COLLATE Latin1_General_CI_AI LIKE CONCAT('%', :keyword, '%') OR " +
            "c.name COLLATE Latin1_General_CI_AI LIKE CONCAT('%', :keyword, '%')) " +
            "ORDER BY r.registration_time DESC",

            // Phải có countQuery vì đây là phân trang (Pageable) với Native Query
            countQuery = "SELECT COUNT(*) FROM registrations r " +
                    "LEFT JOIN users u ON r.user_id = u.id " +
                    "LEFT JOIN courses c ON r.course_id = c.id " +
                    "WHERE (:status IS NULL OR r.status = :status) " +
                    "AND (:keyword IS NULL OR :keyword = '' OR " +
                    "r.order_code COLLATE Latin1_General_CI_AI LIKE CONCAT('%', :keyword, '%') OR " +
                    "u.email COLLATE Latin1_General_CI_AI LIKE CONCAT('%', :keyword, '%') OR " +
                    "c.name COLLATE Latin1_General_CI_AI LIKE CONCAT('%', :keyword, '%'))",

            nativeQuery = true)
    Page<Registration> findOrders(@Param("keyword") String keyword,
                                  @Param("status") String status,
                                  Pageable pageable);
}
