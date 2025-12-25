package com.codecampus.repository;

import com.codecampus.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository quản lý các vai trò người dùng (User Roles) trong hệ thống.
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Integer> {

    /**
     * Tìm kiếm vai trò dựa trên tên định danh (Ví dụ: 'ADMIN', 'STUDENT', 'TEACHER').
     * * @param name Tên vai trò cần tìm
     * @return Đối tượng UserRole bọc trong Optional
     */
    Optional<UserRole> findByName(String name);
}