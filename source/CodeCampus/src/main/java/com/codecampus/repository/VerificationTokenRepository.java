package com.codecampus.repository;

import com.codecampus.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository quản lý các mã xác thực (Verification Token) cho việc kích hoạt tài khoản hoặc đổi mật khẩu.
 */
@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Integer> {

    /**
     * Tìm kiếm thông tin mã xác thực dựa trên chuỗi Token.
     * * @param token Chuỗi mã xác thực duy nhất được gửi qua email
     * @return Đối tượng VerificationToken bọc trong Optional
     */
    Optional<VerificationToken> findByToken(String token);
}