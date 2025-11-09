// src/main/java/com/codecampus/service/RegistrationService.java
package com.codecampus.service;

import com.codecampus.dto.RegistrationRequest;
import com.codecampus.entity.*;
import com.codecampus.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RegistrationService {

    // Tiêm (Inject) tất cả các dependencies cần thiết
    @Autowired private UserRepository userRepository;
    @Autowired private CourseRepository courseRepository;
    @Autowired private PricePackageRepository pricePackageRepository;
    @Autowired private RegistrationRepository registrationRepository;
    @Autowired private UserRoleRepository userRoleRepository;
    @Autowired private PasswordEncoder passwordEncoder; // Cần PasswordEncoder Bean
    @Autowired private EmailService emailService;

    /**
     * BƯỚC 1: Tạo đơn hàng PENDING khi user submit form
     * (Sử dụng DTO và Lombok-setter)
     */
    @Transactional
    public Registration createPendingRegistration(RegistrationRequest request, String loggedInUsername) {

        User user;
        if (loggedInUsername != null) {
            // Case 1: User đã đăng nhập
            user = userRepository.findByEmail(loggedInUsername)
                    .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy user."));
        } else {
            // Case 2: User là khách
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new RuntimeException("Email đã tồn tại. Vui lòng đăng nhập.");
            }
            user = createPendingUser(request); // Tạo user mới (status=pending)
        }

        Course course = courseRepository.findById(request.getCourseId().longValue())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học."));
        PricePackage pricePackage = pricePackageRepository.findById(request.getPackageId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy gói giá."));

        // Tính toán chi phí
        BigDecimal totalCost = pricePackage.getSalePrice() != null
                ? pricePackage.getSalePrice()
                : pricePackage.getListPrice();

        // Tạo đơn đăng ký (sử dụng Lombok setters)
        Registration reg = new Registration();
        reg.setUser(user);
        reg.setCourse(course);
        reg.setPricePackage(pricePackage);
        reg.setTotalCost(totalCost);
        reg.setRegistrationTime(LocalDateTime.now());
        reg.setStatus("PENDING"); // Đơn hàng chờ thanh toán
        reg.setOrderCode("CC-" + System.currentTimeMillis()); // Mã đơn hàng
        reg.setUpdatedAt(LocalDateTime.now());

        return registrationRepository.save(reg);
    }

    /**
     * BƯỚC 2: Hoàn tất đăng ký (khi Webhook gọi)
     */
    @Transactional
    public void completeRegistration(String orderCode) {
        Registration reg = registrationRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Webhook: Không tìm thấy đơn hàng: " + orderCode));

        // Chỉ xử lý nếu đơn hàng đang PENDING (tránh thanh toán 2 lần)
        if ("PENDING".equals(reg.getStatus())) {

            // 1. Cập nhật đơn hàng
            reg.setStatus("COMPLETED");
            reg.setUpdatedAt(LocalDateTime.now());

            LocalDateTime now = LocalDateTime.now();
            int duration = reg.getPricePackage().getDurationMonths();
            reg.setValidFrom(now);
            reg.setValidTo(now.plusMonths(duration));
            registrationRepository.save(reg);

            // 2. Kích hoạt User (nếu đang PENDING)
            User user = reg.getUser();
            if ("PENDING".equals(user.getStatus())) {
                user.setStatus("ACTIVE");
                userRepository.save(user);
            }

            // 3. Gửi email "THANH TOÁN THÀNH CÔNG"
            emailService.sendPaymentSuccessEmail(reg);
        }
    }

    /**
     * BƯỚC 3: Lấy đơn hàng (để JS Polling và hiển thị QR)
     */
    public Registration getRegistrationById(Integer id) {
        return registrationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
    }

    // Hàm private tạo user PENDING (sử dụng DTO và Lombok)
    private User createPendingUser(RegistrationRequest request) {
        // Lấy vai trò mặc định (Giả sử ID 2 là "Student")
        UserRole defaultRole = userRoleRepository.findById(2)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò mặc định (ID=2)."));

        User newUser = new User();
        newUser.setEmail(request.getEmail());
        newUser.setFullName(request.getFullName());
        newUser.setMobile(request.getMobile());
        newUser.setGender(request.getGender());

        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        newUser.setPasswordHash(passwordEncoder.encode(tempPassword));
        newUser.setRole(defaultRole);
        newUser.setStatus("PENDING");
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(newUser);
    }
}