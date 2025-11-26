package com.codecampus.repository;

import com.codecampus.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // Đếm người đăng ký tài khoản mới
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
