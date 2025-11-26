package com.codecampus.repository;

import com.codecampus.entity.User;
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
}
