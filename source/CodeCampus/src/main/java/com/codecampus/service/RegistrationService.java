// src/main/java/com/codecampus/service/RegistrationService.java
package com.codecampus.service;

import com.codecampus.dto.RegistrationRequest;
import com.codecampus.entity.*;
import com.codecampus.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RegistrationService {

    @Autowired private UserRepository userRepository;
    @Autowired private CourseRepository courseRepository;
    @Autowired private PricePackageRepository pricePackageRepository;
    @Autowired private RegistrationRepository registrationRepository;
    @Autowired private EmailService emailService;

    /**
     * (MỚI) HÀM GỘP 2 CHỨC NĂNG: LƯU DB + GỬI MAIL
     * Được gọi khi người dùng bấm "Tôi đã chuyển khoản"
     */
    @Transactional
    public Registration createPendingRegistrationAndSendEmail(RegistrationRequest request, String loggedInUsername) {

        User user = userRepository.findByEmail(loggedInUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy User: " + loggedInUsername));

        // ===== BỔ SUNG: KIỂM TRA TRÙNG LẶP =====
        boolean alreadyPending = registrationRepository.existsByUserIdAndCourseIdAndPricePackageIdAndStatus(
                user.getId(),
                request.getCourseId(),
                request.getPackageId(),
                "PENDING"
        );

        if (alreadyPending) {
            // TRƯỜNG HỢP 1: ĐÃ TỒN TẠI ĐƠN HÀNG PENDING CHO GÓI NÀY
            // Văng lỗi để Controller bắt và thông báo
            throw new RuntimeException("Bạn đã có đơn chờ duyệt cho gói này. Vui lòng kiểm tra 'Khóa học của tôi'.");
        }
        // ======================================

        // TRƯỜNG HỢP 2: CHƯA TỒN TẠI (Tạo đơn mới)

        // SỬA LỖI: Bỏ .longValue() vì Course ID là Integer (theo DBScript)
        Course course = courseRepository.findById(request.getCourseId().longValue())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Khóa học"));

        PricePackage pricePackage = pricePackageRepository.findById(request.getPackageId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Gói giá"));

        BigDecimal totalCost = pricePackage.getSalePrice() != null
                ? pricePackage.getSalePrice()
                : pricePackage.getListPrice();

        // 2. Tạo đối tượng Registration MỚI
        Registration newReg = new Registration();
        newReg.setUser(user);
        newReg.setCourse(course);
        newReg.setPricePackage(pricePackage);
        newReg.setTotalCost(totalCost);
        newReg.setRegistrationTime(LocalDateTime.now());
        newReg.setStatus("PENDING");
        newReg.setOrderCode("CC-" + System.currentTimeMillis());
        newReg.setUpdatedAt(LocalDateTime.now());

        // 3. Lưu vào DB
        registrationRepository.save(newReg);

        // 4. GỬI MAIL "CHỜ DUYỆT"
        emailService.sendRegistrationPendingEmail(newReg);
        System.out.println("Gửi mail chờ duyệt thành công");
        return newReg;
    }

    // 3. Khi Admin bấm "Xác nhận"
    @Transactional
    public void confirmPayment(Integer registrationId) {
        Registration reg = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if ("PENDING".equals(reg.getStatus())) {
            reg.setStatus("COMPLETED");
            reg.setUpdatedAt(LocalDateTime.now());

            LocalDateTime now = LocalDateTime.now();
            int duration = reg.getPricePackage().getDurationMonths();
            reg.setValidFrom(now);
            reg.setValidTo(now.plusMonths(duration));
            registrationRepository.save(reg);

            User user = reg.getUser();
            if ("PENDING".equals(user.getStatus())) {
                user.setStatus("ACTIVE");
                userRepository.save(user);
            }

            emailService.sendPaymentSuccessEmail(reg); // Gửi mail "thành công"
        }
    }
    // BỔ SUNG TÍNH NĂNG HỦY (CANCEL)
    // ==========================================================
    @Transactional
    public void cancelRegistration(Integer registrationId, Integer currentUserId) {
        Registration reg = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng."));

        // Kiểm tra bảo mật: User này có đúng là chủ của đơn hàng không?
        if (!reg.getUser().getId().equals(currentUserId)) {
            throw new RuntimeException("Bạn không có quyền hủy đơn hàng này.");
        }

        // Chỉ cho phép hủy đơn PENDING
        if (!"PENDING".equals(reg.getStatus())) {
            throw new RuntimeException("Không thể hủy đơn hàng đã được xử lý.");
        }

        // Xóa đơn hàng (Logic đơn giản nhất)
        // (Nếu muốn lưu vết, bạn có thể đổi status thành "CANCELLED")
        registrationRepository.delete(reg);
    }

    /**
     * 4. Lấy danh sách cho trang "Khóa học của tôi"
     * (Đây là hàm bạn yêu cầu)
     */
    @Transactional(readOnly = true)
    public List<Registration> getCoursesByUserId(Integer userId, String keyword, Integer categoryId) {
        // Gọi hàm @Query đã được cập nhật
        return registrationRepository.findByUserIdWithDetails(userId, keyword, categoryId);
    }
    // =============================

    /**
     * 5. Lấy đơn hàng (cho trang chờ /pending-approval)
     */
    @Transactional(readOnly = true)
    public Registration getRegistrationByOrderCode(String orderCode) {
        // Gọi hàm JOIN FETCH trong Repository
        return registrationRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
    }

    @Transactional(readOnly = true)
    public boolean hasUserRegistered(Integer userId, Integer courseId) {
        // Gọi phương thức Repository mới, chỉ kiểm tra "COMPLETED"
        return registrationRepository.existsByUserIdAndCourseIdAndStatus(userId, courseId, "COMPLETED");
    }

    /**
     * 7. Lấy đơn hàng (cho Admin sau này)
     */
    @Transactional(readOnly = true)
    public List<Registration> getPendingRegistrations() {
        return registrationRepository.findByStatus("PENDING");
    }


}