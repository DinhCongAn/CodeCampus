package com.codecampus.service;

import com.codecampus.dto.RegistrationDto;
import com.codecampus.entity.User;
import com.codecampus.entity.UserRole;
import com.codecampus.entity.VerificationToken;
import com.codecampus.repository.UserRepository;
import com.codecampus.repository.UserRoleRepository;
import com.codecampus.repository.VerificationTokenRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService;

    public UserService(UserRepository userRepository,
                       UserRoleRepository userRoleRepository,
                       PasswordEncoder passwordEncoder,
                       VerificationTokenRepository tokenRepository,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
    }

    @Transactional
    public void registerNewUser(RegistrationDto dto, String baseUrl) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp.");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email này đã được sử dụng.");
        }

        User user = new User();
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setStatus("pending"); // Trạng thái chờ xác thực

        UserRole defaultRole = userRoleRepository.findByName("STUDENT")
                .orElseThrow(() -> new RuntimeException("Vai trò mặc định 'STUDENT' không tồn tại."));
        user.setRole(defaultRole);

        User savedUser = userRepository.save(user);

        // Tạo token
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(savedUser);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24)); // Hết hạn sau 24h
        tokenRepository.save(verificationToken);

        // Gửi email
        String verificationUrl = baseUrl + "/verify?token=" + token;
        emailService.sendVerificationEmail(
                savedUser.getEmail(),
                "CodeCampus - Xác thực tài khoản của bạn",
                verificationUrl
        );
    }

    public User authenticate(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return null; // Không tìm thấy email
        }

        User user = userOpt.get();

        if (passwordEncoder.matches(password, user.getPasswordHash())) {
            if ("active".equals(user.getStatus())) {
                return user; // Đăng nhập thành công
            }
            // Nếu muốn, có thể ném lỗi cụ thể cho trạng thái 'pending'
            // if ("pending".equals(user.getStatus())) {
            //    throw new RuntimeException("Tài khoản đang chờ xác thực.");
            // }
        }
        return null; // Sai mật khẩu hoặc tài khoản không active
    }

    @Transactional
    public void verifyUser(String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ hoặc không tồn tại."));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(verificationToken); // Xóa token hết hạn
            throw new RuntimeException("Token đã hết hạn. Vui lòng đăng ký lại.");
        }

        User user = verificationToken.getUser();
        user.setStatus("active");
        userRepository.save(user);

        tokenRepository.delete(verificationToken); // Xóa token sau khi đã sử dụng
    }
    // --- PHƯƠNG THỨC MỚI ---
    @Transactional
    public void processForgotPassword(String email, String baseUrl) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        // Chỉ gửi email nếu user tồn tại VÀ đã active
        if (userOpt.isPresent() && "active".equals(userOpt.get().getStatus())) {
            User user = userOpt.get();

            String token = UUID.randomUUID().toString();
            VerificationToken resetToken = new VerificationToken();
            resetToken.setToken(token);
            resetToken.setUser(user);
            resetToken.setExpiryDate(LocalDateTime.now().plusHours(1)); // Hết hạn sau 1h
            tokenRepository.save(resetToken);

            String resetUrl = baseUrl + "/reset-password?token=" + token;
            emailService.sendPasswordResetEmail(user.getEmail(), resetUrl);
        }
        // Lưu ý: Kể cả khi email không tồn tại, chúng ta cũng không báo lỗi
        // để tránh việc hacker dò email.
    }

    // --- PHƯƠNG THỨC MỚI ---
    public Optional<VerificationToken> validatePasswordResetToken(String token) {
        Optional<VerificationToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            return Optional.empty();
        }
        // Kiểm tra hết hạn
        if (tokenOpt.get().getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(tokenOpt.get()); // Xóa token hết hạn
            return Optional.empty();
        }
        return tokenOpt;
    }

    // --- PHƯƠG THỨC MỚI ---
    @Transactional
    public void resetPassword(String token, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp.");
        }

        Optional<VerificationToken> tokenOpt = validatePasswordResetToken(token);
        if (tokenOpt.isEmpty()) {
            throw new RuntimeException("Token không hợp lệ hoặc đã hết hạn.");
        }

        User user = tokenOpt.get().getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Xóa token sau khi đã sử dụng
        tokenRepository.delete(tokenOpt.get());
    }
}