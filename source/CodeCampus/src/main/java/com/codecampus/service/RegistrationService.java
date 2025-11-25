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
     * BƯỚC 1: TẠO ĐƠN HÀNG (PENDING) & LẤY LINK THANH TOÁN
     * - Hàm này KHÔNG kích hoạt khóa học.
     * - Chỉ lưu trạng thái PENDING.
     * - Trả về link để Controller redirect user sang PayOS.
     */
    @Transactional
    public String registerAndGetPaymentUrl(RegistrationRequest request, String loggedInUsername, String returnUrl, String cancelUrl) throws Exception {

        // 1. Kiểm tra User
        User user = userRepository.findByEmail(loggedInUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy User: " + loggedInUsername));

        // 2. Kiểm tra mua trùng (Chỉ chặn nếu đã COMPLETED)
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

        BigDecimal priceBigDec = pricePackage.getSalePrice() != null ? pricePackage.getSalePrice() : pricePackage.getListPrice();
        long finalPrice = priceBigDec.longValue();

        // 4. Tạo OrderCode (Dùng timestamp để đảm bảo duy nhất)
        long orderCodeLong = System.currentTimeMillis();
        String orderCodeString = String.valueOf(orderCodeLong);

        // 5. LƯU DATABASE: BẮT BUỘC LÀ "PENDING"
        Registration newReg = new Registration();
        newReg.setUser(user);
        newReg.setCourse(course);
        newReg.setPricePackage(pricePackage);
        newReg.setTotalCost(priceBigDec);
        newReg.setRegistrationTime(LocalDateTime.now());
        newReg.setStatus("PENDING"); // <--- CHỜ THANH TOÁN
        newReg.setOrderCode(orderCodeString);
        newReg.setUpdatedAt(LocalDateTime.now());

        registrationRepository.save(newReg);

        // 6. GỌI PAYOS TẠO LINK

        // [SỬA LỖI] Tạo mô tả ngắn gọn (Tối đa 25 ký tự)
        // "DH " (3 ký tự) + orderCode (13 ký tự) = 16 ký tự -> OK
        String description = "DH " + orderCodeLong;
        // Đề phòng trường hợp đặc biệt, cắt chuỗi cho chắc chắn
        if (description.length() > 25) {
            description = description.substring(0, 25);
        }

        PaymentLinkItem item = PaymentLinkItem.builder()
                .name(course.getName()) // Đảm bảo getter này đúng với Entity của bạn
                .price(finalPrice)
                .quantity(1)
                .build();

        CreatePaymentLinkRequest payOSRequest = CreatePaymentLinkRequest.builder()
                .orderCode(orderCodeLong)
                .amount(finalPrice)
                .description(description) // <--- Đã dùng biến description ngắn gọn
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .item(item)
                .build();

        CreatePaymentLinkResponse payOSResponse = payOS.paymentRequests().create(payOSRequest);

        return payOSResponse.getCheckoutUrl();
    }

    /**
     * BƯỚC 2: TỰ ĐỘNG KÍCH HOẠT (WEBHOOK)
     * - Đây là nơi DUY NHẤT trong hệ thống được phép chuyển trạng thái thành COMPLETED.
     * - Hàm này được gọi tự động bởi PaymentController khi PayOS báo tiền đã về.
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
            reg.setStatus("COMPLETED"); // <--- KÍCH HOẠT THÀNH CÔNG
            reg.setUpdatedAt(LocalDateTime.now());

            // 2. Tính hạn dùng
            LocalDateTime now = LocalDateTime.now();
            int duration = reg.getPricePackage().getDurationMonths();
            reg.setValidFrom(now);
            reg.setValidTo(now.plusMonths(duration));

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

    // =================================================================
    // CÁC HÀM HỖ TRỢ (READ-ONLY HOẶC HỦY)
    // =================================================================

    /**
     * Hủy đơn hàng (User đổi ý hoặc tạo nhầm)
     * Chỉ cho phép hủy khi đang PENDING.
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
     * [MỚI] Xóa đơn hàng PENDING khi người dùng hủy thanh toán
     * Hàm này kiểm tra kỹ lưỡng để đảm bảo an toàn.
     */
    @Transactional
    public void deletePendingOrder(String orderCode, Integer currentUserId) {
        // 1. Tìm đơn hàng
        Optional<Registration> optionalReg = registrationRepository.findByOrderCode(orderCode);

        if (optionalReg.isPresent()) {
            Registration reg = optionalReg.get();

            // 2. Bảo mật: Kiểm tra xem đơn này có đúng là của User đang đăng nhập không?
            if (!reg.getUser().getId().equals(currentUserId)) {
                // Nếu không phải chính chủ -> Không làm gì cả (hoặc log cảnh báo)
                return;
            }

            // 3. Chỉ xóa nếu trạng thái là PENDING
            if ("PENDING".equals(reg.getStatus())) {
                registrationRepository.delete(reg);
                System.out.println("Đã xóa đơn hàng hủy: " + orderCode);
            }
        }
    }
}