package com.codecampus.service;

import com.codecampus.entity.Registration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    // Lấy email "from" từ properties
    private final String fromEmail = "adinh479@gmail.com"; // Thay bằng email của bạn

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async // Chạy bất đồng bộ
    public void sendVerificationEmail(String toEmail, String subject, String verificationUrl) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);

            String emailBody = "Chào bạn,\n\n"
                    + "Cảm ơn bạn đã đăng ký tài khoản tại CodeCampus.\n"
                    + "Vui lòng nhấp vào liên kết bên dưới để kích hoạt tài khoản của bạn:\n\n"
                    + verificationUrl + "\n\n"
                    + "Lưu ý: Liên kết này sẽ hết hạn sau 24 giờ.\n\n"
                    + "Trân trọng,\n"
                    + "Đội ngũ CodeCampus";

            message.setText(emailBody);

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Lỗi gửi email: " + e.getMessage());
        }
    }
    // --- PHƯƠNG THỨC MỚI ---
    @Async
    public void sendPasswordResetEmail(String toEmail, String resetUrl) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("CodeCampus - Yêu cầu đặt lại mật khẩu");

            String emailBody = "Chào bạn,\n\n"
                    + "Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.\n"
                    + "Vui lòng nhấp vào liên kết bên dưới để đặt lại mật khẩu:\n\n"
                    + resetUrl + "\n\n"
                    + "Lưu ý: Liên kết này sẽ hết hạn sau 1 giờ.\n"
                    + "Nếu bạn không yêu cầu, vui lòng bỏ qua email này.\n\n"
                    + "Trân trọng,\n"
                    + "Đội ngũ CodeCampus";

            message.setText(emailBody);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Lỗi gửi email reset mật khẩu: " + e.getMessage());
        }
    }
    /**
     * Mail 1: Gửi khi người dùng nhấn "Tôi đã chuyển khoản"
     */
    @Async
    public void sendRegistrationPendingEmail(Registration registration) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(registration.getUser().getEmail());
            message.setSubject("CodeCampus - Đăng ký của bạn đang chờ xét duyệt");

            String emailBody = "Chào " + registration.getUser().getFullName() + ",\n\n"
                    + "Cảm ơn bạn đã xác nhận thanh toán cho khóa học: " + registration.getCourse().getName() + ".\n"
                    + "Mã đơn hàng của bạn là: " + registration.getOrderCode() + "\n\n"
                    + "Trạng thái hiện tại: Đang chờ xét duyệt.\n\n"
                    + "Chúng tôi sẽ kích hoạt khóa học của bạn ngay khi xác nhận được thanh toán (thường trong vài giờ làm việc).\n\n"
                    + "Trân trọng,\n"
                    + "Đội ngũ CodeCampus";

            message.setText(emailBody);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Lỗi gửi email chờ duyệt: " + e.getMessage());
        }
    }

    /**
     * Mail 2: Gửi khi Admin nhấn "Xác nhận"
     */
    @Async
    public void sendPaymentSuccessEmail(Registration registration) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(registration.getUser().getEmail());
            message.setSubject("CodeCampus - Đăng ký khóa học thành công!");

            String emailBody = "Chào " + registration.getUser().getFullName() + ",\n\n"
                    + "Khóa học của bạn đã được kích hoạt thành công!\n"
                    + "Khóa học: " + registration.getCourse().getName() + ".\n"
                    + "Mã đơn hàng: " + registration.getOrderCode() + "\n"
                    + "Thời hạn: " + registration.getValidFrom().toLocalDate()
                    + " đến " + registration.getValidTo().toLocalDate() + ".\n\n"
                    + "Bắt đầu học ngay!\n"
                    + "Đội ngũ CodeCampus";

            message.setText(emailBody);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Lỗi gửi email thành công: " + e.getMessage());
        }
    }
}