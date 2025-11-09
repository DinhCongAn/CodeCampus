package com.codecampus.repository;

import com.codecampus.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Integer> {
    // Rất quan trọng: Tìm đơn hàng bằng mã order_code (để Webhook xử lý)
    Optional<Registration> findByOrderCode(String orderCode);
}
