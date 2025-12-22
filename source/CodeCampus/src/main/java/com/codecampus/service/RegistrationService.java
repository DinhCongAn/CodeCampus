package com.codecampus.service;

import com.codecampus.dto.RegistrationRequest;
import com.codecampus.entity.*;
import com.codecampus.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Import PayOS
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;

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

    // Inject PayOS Bean
    @Autowired private PayOS payOS;

    /**
     * BƯỚC 1: XỬ LÝ ĐĂNG KÝ TRUNG TÂM (Miễn phí & Trả phí)
     * - Tự động phát hiện giá tiền.
     * - Nếu giá > 0: Tạo đơn PENDING -> Gọi PayOS -> Trả về link thanh toán.
     * - Nếu giá = 0: Tạo đơn COMPLETED -> Kích hoạt ngay -> Trả về link nội bộ.
     */
    @Transactional
    public String registerAndGetPaymentUrl(RegistrationRequest request, String loggedInUsername, String returnUrl, String cancelUrl) throws Exception {

        // 1. Kiểm tra User
        User user = userRepository.findByEmail(loggedInUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy User: " + loggedInUsername));

        // 2. Kiểm tra mua trùng (Chỉ chặn nếu đã COMPLETED - Đã sở hữu)
        boolean alreadyBought = registrationRepository.existsByUserIdAndCourseIdAndStatus(
                user.getId(), request.getCourseId(), "COMPLETED");
        if (alreadyBought) {
            throw new RuntimeException("Bạn đã sở hữu khóa học này rồi.");
        }

        // 3. Lấy thông tin Course & Package
        Course course = courseRepository.findById(request.getCourseId().longValue())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học"));

        PricePackage pricePackage = pricePackageRepository.findById(request.getPackageId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy gói giá"));

        // Tính giá tiền cuối cùng (Ưu tiên giá Sale)
        BigDecimal priceBigDec = pricePackage.getSalePrice() != null ? pricePackage.getSalePrice() : pricePackage.getListPrice();
        long finalPrice = (priceBigDec != null) ? priceBigDec.longValue() : 0;

        // 4. Tạo OrderCode (Dùng timestamp để đảm bảo duy nhất)
        long orderCodeLong = System.currentTimeMillis();
        String orderCodeString = String.valueOf(orderCodeLong);

        // 5. Khởi tạo đối tượng Registration (Chưa lưu vội)
        Registration newReg = new Registration();
        newReg.setUser(user);
        newReg.setCourse(course);
        newReg.setPricePackage(pricePackage);
        newReg.setTotalCost(priceBigDec);
        newReg.setRegistrationTime(LocalDateTime.now());
        newReg.setOrderCode(orderCodeString);
        newReg.setUpdatedAt(LocalDateTime.now());

        // ============================================================
        // NHÁNH 1: MIỄN PHÍ (GIÁ <= 0) -> KÍCH HOẠT NGAY
        // ============================================================
        if (finalPrice <= 0) {
            newReg.setStatus("COMPLETED"); // Thành công ngay

            // Tính toán ngày hết hạn (Tái sử dụng logic)
            calculateAndSetExpiry(newReg);

            registrationRepository.save(newReg);

            // Gửi email xác nhận (Tùy chọn)
            // emailService.sendEnrollmentSuccessEmail(newReg);

            // Trả về URL thành công nội bộ (Controller sẽ hứng và báo success)
            return returnUrl;
        }

        // ============================================================
        // NHÁNH 2: TRẢ PHÍ (GIÁ > 0) -> GỌI PAYOS
        // ============================================================
        newReg.setStatus("PENDING"); // Chờ thanh toán
        registrationRepository.save(newReg);

        // Tạo mô tả ngắn gọn cho PayOS
        String description = "DH " + orderCodeLong;
        if (description.length() > 25) {
            description = description.substring(0, 25);
        }

        PaymentLinkItem item = PaymentLinkItem.builder()
                .name(course.getName())
                .price(finalPrice)
                .quantity(1)
                .build();

        CreatePaymentLinkRequest payOSRequest = CreatePaymentLinkRequest.builder()
                .orderCode(orderCodeLong)
                .amount(finalPrice)
                .description(description)
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .item(item)
                .build();

        CreatePaymentLinkResponse payOSResponse = payOS.paymentRequests().create(payOSRequest);

        return payOSResponse.getCheckoutUrl();
    }

    /**
     * BƯỚC 2: TỰ ĐỘNG KÍCH HOẠT (WEBHOOK PAYOS)
     * - Chỉ dùng cho đơn hàng TRẢ PHÍ khi PayOS báo tiền đã về.
     */
    @Transactional
    public void activateRegistration(long orderCode) {
        String orderCodeStr = String.valueOf(orderCode);

        // Tìm đơn hàng
        Registration reg = registrationRepository.findByOrderCode(orderCodeStr)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng mã: " + orderCode));

        // Kiểm tra an toàn: Chỉ xử lý nếu đang PENDING
        if ("PENDING".equals(reg.getStatus())) {
            // 1. Update trạng thái
            reg.setStatus("COMPLETED");
            reg.setUpdatedAt(LocalDateTime.now());

            // 2. Tính hạn dùng (Tái sử dụng logic)
            calculateAndSetExpiry(reg);

            registrationRepository.save(reg);

            // 3. Active User (nếu cần)
            User user = reg.getUser();
            if ("PENDING".equals(user.getStatus())) {
                user.setStatus("ACTIVE");
                userRepository.save(user);
            }

            // 4. Gửi mail
            emailService.sendPaymentSuccessEmail(reg);

            System.out.println(">>> AUTO-ACTIVATED Order: " + orderCode);
        } else {
            System.out.println(">>> Order " + orderCode + " ignored (Status: " + reg.getStatus() + ")");
        }
    }

    /**
     * [HELPER] Logic tính toán ngày hết hạn (Dùng chung cho cả 2 luồng)
     * Giúp code gọn gàng và nhất quán.
     */
    private void calculateAndSetExpiry(Registration reg) {
        LocalDateTime now = LocalDateTime.now();
        int durationMonths = reg.getPricePackage().getDurationMonths();

        reg.setValidFrom(now);
        // Cộng thời hạn vào ngày hiện tại
        reg.setValidTo(now.plusMonths(durationMonths));
    }

    // =================================================================
    // CÁC HÀM HỖ TRỢ KHÁC (GIỮ NGUYÊN)
    // =================================================================

    /**
     * Hủy đơn hàng (User đổi ý hoặc tạo nhầm)
     */
    @Transactional
    public void cancelRegistration(Integer registrationId, Integer currentUserId) {
        Registration reg = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng."));

        if (!reg.getUser().getId().equals(currentUserId)) {
            throw new RuntimeException("Bạn không chính chủ.");
        }

        if ("COMPLETED".equals(reg.getStatus())) {
            throw new RuntimeException("Không thể hủy đơn hàng đã thanh toán thành công.");
        }

        registrationRepository.delete(reg);
    }

    @Transactional(readOnly = true)
    public List<Registration> getCoursesByUserId(Integer userId, String keyword, Integer categoryId) {
        return registrationRepository.findByUserIdWithDetails(userId, keyword, categoryId);
    }

    @Transactional(readOnly = true)
    public Registration getRegistrationByOrderCode(String orderCode) {
        return registrationRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
    }

    @Transactional(readOnly = true)
    public boolean hasUserRegistered(Integer userId, Integer courseId) {
        return registrationRepository.existsByUserIdAndCourseIdAndStatus(userId, courseId, "COMPLETED");
    }

    /**
     * Xóa đơn hàng PENDING khi người dùng hủy thanh toán
     */
    @Transactional
    public void deletePendingOrder(String orderCode, Integer currentUserId) {
        Optional<Registration> optionalReg = registrationRepository.findByOrderCode(orderCode);

        if (optionalReg.isPresent()) {
            Registration reg = optionalReg.get();
            // Bảo mật: Đúng chủ và đúng là Pending mới xóa
            if (reg.getUser().getId().equals(currentUserId) && "PENDING".equals(reg.getStatus())) {
                registrationRepository.delete(reg);
                System.out.println("Đã xóa đơn hàng hủy: " + orderCode);
            }
        }
    }
}