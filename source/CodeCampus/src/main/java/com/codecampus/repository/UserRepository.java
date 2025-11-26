package com.codecampus.repository;

import com.codecampus.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // Đếm người đăng ký tài khoản mới
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // Hàm tìm User có Role nằm trong danh sách cho trước (Ví dụ: ADMIN, EXPERT)
    @Query("SELECT u FROM User u WHERE u.role.name IN :roleNames")
    List<User> findByRoleNames(@Param("roleNames") List<String> roleNames);

    // 3. TÌM KIẾM NÂNG CAO CHO ADMIN (NATIVE QUERY SQL SERVER)
    // - Keyword: Tìm trong Tên, Email, SĐT
    // - RoleId: Lọc theo quyền
    // - Status: Lọc theo trạng thái
    @Query(value = "SELECT u.* FROM users u " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR " +
            "u.full_name COLLATE Latin1_General_CI_AI LIKE CONCAT('%', :keyword, '%') OR " +
            "u.email COLLATE Latin1_General_CI_AI LIKE CONCAT('%', :keyword, '%') OR " +
            "u.mobile COLLATE Latin1_General_CI_AI LIKE CONCAT('%', :keyword, '%')) " +
            "AND (:roleId IS NULL OR u.role_id = :roleId) " +
            "AND (:status IS NULL OR :status = '' OR u.status = :status) " +
            "ORDER BY u.created_at DESC",

            // Bắt buộc phải có countQuery khi dùng Pageable với Native Query
            countQuery = "SELECT COUNT(*) FROM users u " +
                    "WHERE (:keyword IS NULL OR :keyword = '' OR " +
                    "u.full_name COLLATE Latin1_General_CI_AI LIKE CONCAT('%', :keyword, '%') OR " +
                    "u.email COLLATE Latin1_General_CI_AI LIKE CONCAT('%', :keyword, '%') OR " +
                    "u.mobile COLLATE Latin1_General_CI_AI LIKE CONCAT('%', :keyword, '%')) " +
                    "AND (:roleId IS NULL OR u.role_id = :roleId) " +
                    "AND (:status IS NULL OR :status = '' OR u.status = :status)",

            nativeQuery = true)
    Page<User> findUsersAdmin(@Param("keyword") String keyword,
                              @Param("roleId") Integer roleId,
                              @Param("status") String status,
                              Pageable pageable);

}
