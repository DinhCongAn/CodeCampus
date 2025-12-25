package com.codecampus.repository;

import com.codecampus.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository quản lý thông tin người dùng (User).
 * Hỗ trợ các chức năng đăng nhập, phân quyền và quản trị người dùng nâng cao.
 * Đảm bảo tương thích 100% giữa SQL Server (Local) và TiDB/MySQL (Render).
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * Tìm kiếm người dùng bằng Email (thường dùng cho chức năng Đăng nhập/Security).
     */
    Optional<User> findByEmail(String email);

    /**
     * Kiểm tra sự tồn tại của Email trong hệ thống.
     */
    boolean existsByEmail(String email);

    /**
     * Thống kê số lượng người dùng mới đăng ký trong một khoảng thời gian.
     */
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Lấy danh sách người dùng dựa trên danh sách tên quyền (Roles).
     * Ví dụ: Lấy tất cả người dùng có quyền 'ADMIN' hoặc 'EXPERT'.
     */
    @Query("SELECT u FROM User u WHERE u.role.name IN :roleNames")
    List<User> findByRoleNames(@Param("roleNames") List<String> roleNames);

    /**
     * Truy vấn tìm kiếm và lọc danh sách người dùng cho giao diện Quản trị (Admin).
     * GIẢI PHÁP TƯƠNG THÍCH:
     * 1. Đã chuyển từ Native Query sang JPQL để loại bỏ 'COLLATE Latin1_General_CI_AI'.
     * 2. Sử dụng LOWER() và CONCAT() để tìm kiếm không phân biệt hoa thường trên mọi DB.
     * 3. Truy cập Role ID thông qua u.role.id (Object Navigation) thay vì u.role_id (Native Column).
     * * @param keyword Từ khóa tìm kiếm (Tên, Email hoặc Số điện thoại)
     * @param roleId ID của quyền cần lọc
     * @param status Trạng thái tài khoản (active, inactive, v.v.)
     * @param pageable Tham số phân trang và sắp xếp
     */
    @Query("SELECT u FROM User u WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.mobile) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:roleId IS NULL OR u.role.id = :roleId) " +
            "AND (:status IS NULL OR :status = '' OR u.status = :status) " +
            "ORDER BY u.createdAt DESC")
    Page<User> findUsersAdmin(@Param("keyword") String keyword,
                              @Param("roleId") Integer roleId,
                              @Param("status") String status,
                              Pageable pageable);

}