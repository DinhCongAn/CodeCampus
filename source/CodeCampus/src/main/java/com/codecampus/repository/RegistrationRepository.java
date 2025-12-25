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
     * Lấy thông tin đăng ký theo mã đơn hàng, nạp sẵn User và Course.
     */
    @Query("SELECT r FROM Registration r JOIN FETCH r.user JOIN FETCH r.course WHERE r.orderCode = :orderCode")
    Optional<Registration> findByOrderCode(@Param("orderCode") String orderCode);

    /**
     * Kiểm tra người dùng đã đăng ký khóa học chưa.
     */
    boolean existsByUserIdAndCourseId(Integer userId, Integer courseId);

    /**
     * Lấy danh sách đăng ký kèm chi tiết cho trang Khóa học của tôi.
     */
    @Query("SELECT r FROM Registration r " +
            "JOIN FETCH r.user u " +
            "JOIN FETCH r.course c " +
            "JOIN FETCH r.pricePackage p " +
            "JOIN c.category cat " +
            "WHERE u.id = :userId " +
            "AND (:keyword IS NULL OR :keyword = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:categoryId IS NULL OR cat.id = :categoryId) " +
            "ORDER BY r.registrationTime DESC")
    List<Registration> findByUserIdWithDetails(
            @Param("userId") Integer userId,
            @Param("keyword") String keyword,
            @Param("categoryId") Integer categoryId
    );

    /**
     * Tìm danh sách đơn hàng theo trạng thái.
     */
    List<Registration> findByStatus(String status);

    /**
     * Kiểm tra trạng thái đăng ký của user trong khóa học.
     */
    boolean existsByUserIdAndCourseIdAndStatus(Integer userId, Integer courseId, String status);

    /**
     * Kiểm tra đơn hàng đang chờ duyệt cho gói giá cụ thể.
     */
    boolean existsByUserIdAndCourseIdAndPricePackageIdAndStatus(
            Integer userId, Integer courseId, Integer pricePackageId, String status
    );

    /**
     * Tìm bản ghi đăng ký của user cho khóa học.
     */
    Registration findByUserIdAndCourseId(Integer userId, Integer courseId);

    /**
     * Tìm kiếm và lọc danh sách đăng ký.
     */
    @Query("SELECT r FROM Registration r WHERE r.user.id = :userId " +
            "AND (:keyword IS NULL OR :keyword = '' OR LOWER(r.course.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:categoryId IS NULL OR r.course.category.id = :categoryId)")
    List<Registration> findByUserIdAndFilters(@Param("userId") Integer userId,
                                              @Param("keyword") String keyword,
                                              @Param("categoryId") Integer categoryId);

    /**
     * Tính tổng doanh thu thành công trong khoảng thời gian.
     */
    @Query("SELECT COALESCE(SUM(r.totalCost), 0) FROM Registration r " +
            "WHERE r.status = 'COMPLETED' AND r.registrationTime BETWEEN :start AND :end")
    BigDecimal sumRevenue(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Đếm số đơn hàng theo trạng thái trong kỳ.
     */
    @Query("SELECT COUNT(r) FROM Registration r " +
            "WHERE r.status = :status AND r.registrationTime BETWEEN :start AND :end")
    Long countByStatus(@Param("status") String status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Đếm tất cả đơn hàng phát sinh trong kỳ.
     */
    @Query("SELECT COUNT(r) FROM Registration r WHERE r.registrationTime BETWEEN :start AND :end")
    Long countAllInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Lấy dữ liệu biểu đồ xu hướng (Tổng số đơn theo ngày).
     */
    @Query("SELECT CAST(r.registrationTime AS date), COUNT(r) FROM Registration r " +
            "WHERE r.registrationTime BETWEEN :start AND :end " +
            "GROUP BY CAST(r.registrationTime AS date) ORDER BY CAST(r.registrationTime AS date)")
    List<Object[]> countOrdersByDay(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Lấy dữ liệu biểu đồ xu hướng (Đơn thành công theo ngày).
     */
    @Query("SELECT CAST(r.registrationTime AS date), COUNT(r) FROM Registration r " +
            "WHERE r.status = 'COMPLETED' AND r.registrationTime BETWEEN :start AND :end " +
            "GROUP BY CAST(r.registrationTime AS date) ORDER BY CAST(r.registrationTime AS date)")
    List<Object[]> countSuccessOrdersByDay(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Doanh thu phân bổ theo danh mục.
     */
    @Query("SELECT c.category.name, SUM(r.totalCost) FROM Registration r " +
            "JOIN r.course c " +
            "WHERE r.status = 'COMPLETED' AND r.registrationTime BETWEEN :start AND :end " +
            "GROUP BY c.category.name")
    List<Object[]> getRevenueByCategory(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Lấy danh sách Top khóa học bán chạy.
     */
    @Query("SELECT new com.codecampus.dto.TopSubjectDTO(c.name, c.category.name, COUNT(r), SUM(r.totalCost)) " +
            "FROM Registration r JOIN r.course c " +
            "WHERE r.status = 'COMPLETED' AND r.registrationTime BETWEEN :start AND :end " +
            "GROUP BY c.id, c.name, c.category.name " +
            "ORDER BY SUM(r.totalCost) DESC")
    List<TopSubjectDTO> getTopSubjects(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    /**
     * Đếm số khách hàng mới mua lần đầu trong kỳ.
     */
    @Query("SELECT COUNT(DISTINCT r.user) FROM Registration r WHERE r.status = 'COMPLETED' " +
            "AND r.registrationTime BETWEEN :start AND :end " +
            "AND r.user.id NOT IN (SELECT r2.user.id FROM Registration r2 WHERE r2.status = 'COMPLETED' AND r2.registrationTime < :start)")
    Long countNewPayingCustomers(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Tìm kiếm và phân trang đơn hàng cho Admin.
     * Tương thích SQL Server/TiDB bằng cách thay thế Native Query COLLATE bằng JPQL LOWER.
     */
    @Query("SELECT r FROM Registration r " +
            "LEFT JOIN r.user u " +
            "LEFT JOIN r.course c " +
            "WHERE (:status IS NULL OR r.status = :status) " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(r.orderCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY r.registrationTime DESC")
    Page<Registration> findOrders(@Param("keyword") String keyword,
                                  @Param("status") String status,
                                  Pageable pageable);

    /**
     * Thống kê tổng tiền doanh thu theo từng ngày.
     */
    @Query("SELECT CAST(r.registrationTime AS date), SUM(r.totalCost) " +
            "FROM Registration r " +
            "WHERE r.status = 'COMPLETED' AND r.registrationTime BETWEEN :start AND :end " +
            "GROUP BY CAST(r.registrationTime AS date) ORDER BY CAST(r.registrationTime AS date)")
    List<Object[]> getRevenueByDate(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Lấy danh sách chi tiết giao dịch thành công.
     */
    @Query("SELECT r FROM Registration r " +
            "WHERE r.status = 'COMPLETED' " +
            "AND r.registrationTime BETWEEN :start AND :end " +
            "ORDER BY r.registrationTime DESC")
    Page<Registration> findSuccessfulOrders(@Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end,
                                            Pageable pageable);
}