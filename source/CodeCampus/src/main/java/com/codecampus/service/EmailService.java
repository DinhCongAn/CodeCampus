package com.codecampus.service;

import com.codecampus.dto.ContactDTO;
import com.codecampus.entity.Registration;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    // Lấy email từ application.properties để tránh hardcode
    @Value("${spring.mail.username:adinh479@gmail.com}")
    private String fromEmail;

    // Email admin nhận thông báo liên hệ
    private final String adminEmail = "andche186895@fpt.edu.vn";

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * 1. Gửi Email Xác thực Tài khoản
     */
    @Async
    public void sendVerificationEmail(String toEmail, String subject, String verificationUrl) {
        String content = """
            <h2 style="color: #1e293b;">Xác thực tài khoản</h2>
            <p>Chào bạn,</p>
            <p>Cảm ơn bạn đã đăng ký tham gia cộng đồng <strong>CodeCampus</strong>. Để bắt đầu hành trình học tập, vui lòng xác thực địa chỉ email của bạn.</p>
            <p style="text-align: center; margin: 30px 0;">
                <a href="%s" class="btn">Kích hoạt tài khoản ngay</a>
            </p>
            <p style="color: #64748b; font-size: 14px;">Lưu ý: Liên kết này sẽ hết hạn sau 24 giờ vì lý do bảo mật.</p>
        """.formatted(verificationUrl);

        sendHtmlEmail(toEmail, subject, content);
    }

    /**
     * 2. Gửi Email Đặt lại Mật khẩu
     */
    @Async
    public void sendPasswordResetEmail(String toEmail, String resetUrl) {
        String content = """
            <h2 style="color: #1e293b;">Yêu cầu đặt lại mật khẩu</h2>
            <p>Chào bạn,</p>
            <p>Chúng tôi nhận được yêu cầu khôi phục mật khẩu cho tài khoản CodeCampus liên kết với email này.</p>
            <p style="text-align: center; margin: 30px 0;">
                <a href="%s" class="btn" style="background-color: #ef4444;">Đặt lại mật khẩu</a>
            </p>
            <p style="color: #64748b; font-size: 14px;">Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này. Tài khoản của bạn vẫn an toàn.</p>
            <p style="color: #64748b; font-size: 14px;">Liên kết có hiệu lực trong 1 giờ.</p>
        """.formatted(resetUrl);

        sendHtmlEmail(toEmail, "CodeCampus - Đặt lại mật khẩu", content);
    }

    /**
     * 3. Gửi Email Thanh toán Thành công
     */
    @Async
    public void sendPaymentSuccessEmail(Registration registration) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DecimalFormat decimalFormat = new DecimalFormat("###,###,###");

        String courseName = registration.getCourse().getName();
        String orderCode = registration.getOrderCode();
        String validFrom = registration.getValidFrom().format(formatter);
        String validTo = registration.getValidTo().format(formatter);
        String price = decimalFormat.format(registration.getTotalCost());

        String content = """
            <h2 style="color: #10b981;">Thanh toán thành công! 🎉</h2>
            <p>Chào <strong>%s</strong>,</p>
            <p>Đơn hàng đăng ký khóa học của bạn đã được xác nhận. Bạn có thể bắt đầu học ngay bây giờ!</p>
            
            <div style="background-color: #f1f5f9; padding: 20px; border-radius: 12px; margin: 20px 0;">
                <h3 style="margin-top: 0; color: #4361ee;">Thông tin đơn hàng #%s</h3>
                <ul style="list-style: none; padding: 0; margin: 0;">
                    <li style="margin-bottom: 10px;">📚 <strong>Khóa học:</strong> %s</li>
                    <li style="margin-bottom: 10px;">💰 <strong>Học phí:</strong> %s VNĐ</li>
                    <li style="margin-bottom: 10px;">📅 <strong>Hiệu lực:</strong> %s - %s</li>
                </ul>
            </div>
            
            <p style="text-align: center; margin: 30px 0;">
                <a href="http://localhost:8080/my-courses" class="btn">Vào học ngay</a>
            </p>
        """.formatted(registration.getUser().getFullName(), orderCode, courseName, price, validFrom, validTo);

        sendHtmlEmail(registration.getUser().getEmail(), "CodeCampus - Kích hoạt khóa học thành công", content);
    }

    /**
     * 4. Gửi Email Thông tin Tài khoản Mới (Cấp bởi Admin)
     */
    @Async
    public void sendNewAccountEmail(String toEmail, String fullName, String randomPassword) {
        String content = """
            <h2 style="color: #1e293b;">Chào mừng đến với CodeCampus!</h2>
            <p>Xin chào <strong>%s</strong>,</p>
            <p>Tài khoản của bạn đã được khởi tạo thành công trên hệ thống.</p>
            
            <div style="background-color: #eff6ff; border: 1px solid #bfdbfe; padding: 20px; border-radius: 12px; margin: 20px 0;">
                <p style="margin: 0 0 10px 0;"><strong>📧 Email đăng nhập:</strong> %s</p>
                <p style="margin: 0;"><strong>🔑 Mật khẩu tạm thời:</strong> <span style="font-family: monospace; font-size: 16px; background: white; padding: 2px 8px; rounded: 4px;">%s</span></p>
            </div>
            
            <p>Vui lòng đăng nhập và đổi mật khẩu ngay trong lần truy cập đầu tiên để bảo mật tài khoản.</p>
            
            <p style="text-align: center; margin: 30px 0;">
                <a href="http://localhost:8080/login" class="btn">Đăng nhập ngay</a>
            </p>
        """.formatted(fullName, toEmail, randomPassword);

        sendHtmlEmail(toEmail, "CodeCampus - Thông tin tài khoản mới", content);
    }

    /**
     * 5. Gửi Email Liên hệ (Gửi cho Admin)
     */
    @Async
    public void sendContactEmail(ContactDTO contact) {
        String content = """
            <h2 style="color: #1e293b;">📩 Liên hệ mới từ Website</h2>
            <table style="width: 100%%; border-collapse: collapse;">
                <tr>
                    <td style="padding: 8px 0; border-bottom: 1px solid #e2e8f0; width: 120px;"><strong>Người gửi:</strong></td>
                    <td style="padding: 8px 0; border-bottom: 1px solid #e2e8f0;">%s</td>
                </tr>
                <tr>
                    <td style="padding: 8px 0; border-bottom: 1px solid #e2e8f0;"><strong>Email:</strong></td>
                    <td style="padding: 8px 0; border-bottom: 1px solid #e2e8f0;">%s</td>
                </tr>
                <tr>
                    <td style="padding: 8px 0; border-bottom: 1px solid #e2e8f0;"><strong>Chủ đề:</strong></td>
                    <td style="padding: 8px 0; border-bottom: 1px solid #e2e8f0;">%s</td>
                </tr>
            </table>
            
            <div style="background-color: #f8fafc; padding: 20px; border-radius: 8px; margin-top: 20px; border: 1px solid #e2e8f0;">
                <strong>Nội dung tin nhắn:</strong><br><br>
                %s
            </div>
        """.formatted(contact.getFullName(), contact.getEmail(), contact.getSubject(), contact.getMessage().replace("\n", "<br>"));

        sendHtmlEmail(adminEmail, "[Contact] " + contact.getSubject(), content);
    }

    /**
     * 6. Gửi Email Thông báo Đăng ký Nhận tin (Newsletter) - MỚI THÊM
     */
    @Async
    public void sendSubscriptionEmail(String userEmail) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"));

        String content = """
            <h2 style="color: #1e293b;">🔔 Đăng ký nhận tin mới</h2>
            <p>Hệ thống vừa ghi nhận một người dùng đăng ký nhận bản tin (Newsletter).</p>
            
            <div style="background-color: #f0fdf4; border: 1px solid #bbf7d0; padding: 20px; border-radius: 12px; margin: 20px 0;">
                <p style="margin: 0 0 10px 0;"><strong>📧 Email đăng ký:</strong> <a href="mailto:%s" style="color: #16a34a; text-decoration: none; font-weight: bold;">%s</a></p>
                <p style="margin: 0;"><strong>⏰ Thời gian:</strong> %s</p>
            </div>
            
            <p style="color: #64748b; font-size: 14px;">Email này được gửi tự động từ hệ thống CodeCampus.</p>
        """.formatted(userEmail, userEmail, time);

        sendHtmlEmail(adminEmail, "[Newsletter] Có người đăng ký mới: " + userEmail, content);
    }

    /**
     * ==========================================
     * CORE: HÀM GỬI HTML EMAIL CHUNG (FRAMEWORK)
     * ==========================================
     */
    private void sendHtmlEmail(String to, String subject, String bodyContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "CodeCampus Support");
            helper.setTo(to);
            helper.setSubject(subject);

            // Template HTML chuẩn Responsive
            String htmlTemplate = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; background-color: #f1f5f9; }
                        .email-container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.05); margin-top: 20px; margin-bottom: 20px; }
                        .header { background: linear-gradient(135deg, #4361ee 0%%, #3a0ca3 100%%); padding: 30px 20px; text-align: center; }
                        .header h1 { margin: 0; color: #ffffff; font-size: 24px; letter-spacing: 1px; font-weight: 700; }
                        .content { padding: 40px 30px; color: #334155; line-height: 1.6; font-size: 16px; }
                        .footer { background-color: #f8fafc; padding: 20px; text-align: center; font-size: 12px; color: #94a3b8; border-top: 1px solid #e2e8f0; }
                        .btn { display: inline-block; padding: 12px 30px; background-color: #4361ee; color: #ffffff !important; text-decoration: none; border-radius: 50px; font-weight: 600; box-shadow: 0 4px 6px rgba(67, 97, 238, 0.25); }
                        .btn:hover { background-color: #3f37c9; }
                        a { color: #4361ee; text-decoration: none; }
                    </style>
                </head>
                <body>
                    <div class="email-container">
                        <!-- Header Logo -->
                        <div class="header">
                            <h1>CodeCampus</h1>
                        </div>
                        
                        <!-- Main Content -->
                        <div class="content">
                            %s
                        </div>
                        
                        <!-- Footer -->
                        <div class="footer">
                            <p>&copy; 2025 CodeCampus Inc. All rights reserved.</p>
                            <p>Tòa nhà TechHub, Số 1 Đại Cồ Việt, Hai Bà Trưng, Hà Nội</p>
                            <p>Email này được gửi tự động, vui lòng không trả lời trực tiếp.</p>
                        </div>
                    </div>
                </body>
                </html>
            """.formatted(bodyContent);

            helper.setText(htmlTemplate, true); // true = html mode

            mailSender.send(message);
            System.out.println("✅ Đã gửi email thành công đến: " + to);

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            System.err.println("❌ Lỗi gửi email: " + e.getMessage());
        }
    }
}