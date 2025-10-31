package com.codecampus.service;

import com.codecampus.dto.PasswordDto;
import com.codecampus.dto.RegistrationDto;
import com.codecampus.entity.User;
import com.codecampus.entity.UserRole;
import com.codecampus.entity.VerificationToken;
import com.codecampus.repository.UserRepository;
import com.codecampus.repository.UserRoleRepository;
import com.codecampus.repository.VerificationTokenRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private static final String UPLOAD_DIR = "src/main/resources/static/avatars/";
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
        // Đảm bảo thư mục UPLOAD_DIR tồn tại khi Service được khởi tạo
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            System.err.println("Không thể tạo thư mục upload avatar: " + e.getMessage());
        }
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
    // --- PHƯƠNG THỨC MỚI CHO GOOGLE LOGIN ---
    @Transactional
    public User processOAuthPostLogin(String email, String fullName) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            // 1. User đã tồn tại: Cập nhật thông tin và trả về
            User existingUser = userOpt.get();
            existingUser.setFullName(fullName);
            // Bạn có thể thêm cột avatar và cập nhật ở đây
            return userRepository.save(existingUser);
        } else {
            // 2. User mới: Tạo tài khoản mới
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFullName(fullName);
            // Trạng thái 'active' ngay vì Google đã xác thực
            newUser.setStatus("active");
            // Tạo mật khẩu ngẫu nhiên (vì cột password_hash là NOT NULL)
            newUser.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));

            UserRole defaultRole = userRoleRepository.findByName("STUDENT")
                    .orElseThrow(() -> new RuntimeException("Vai trò 'STUDENT' không tồn tại."));
            newUser.setRole(defaultRole);

            return userRepository.save(newUser);
        }
    }

    public User findUserByEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        return userOpt.orElse(null); // Trả về null nếu không tìm thấy
    }

    // --- PHƯƠNG THỨC MỚI 1: CẬP NHẬT THÔNG TIN CÁ NHÂN ---
    @Transactional
    public User updateProfile(User user,
                              String newFullName,
                              String newMobile,
                              String newGender,
                              String newAddress) {

        // Cập nhật các trường
        user.setFullName(newFullName);
        user.setMobile(newMobile);
        user.setGender(newGender);
        user.setAddress(newAddress);

        // Logic @PreUpdate trong User.java sẽ tự cập nhật updatedAt
        return userRepository.save(user);
    }

    // --- PHƯƠNG THỨC MỚI 2: ĐỔI MẬT KHẨU ---
    @Transactional
    public void changePassword(String email, PasswordDto passwordDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User không tồn tại."));

        // 1. Kiểm tra mật khẩu cũ
        if (!passwordEncoder.matches(passwordDto.getOldPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Mật khẩu cũ không chính xác.");
        }

        // 2. Kiểm tra mật khẩu mới và xác nhận
        if (!passwordDto.getNewPassword().equals(passwordDto.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu mới và xác nhận mật khẩu không khớp.");
        }

        // 3. Mã hóa và lưu mật khẩu mới
        user.setPasswordHash(passwordEncoder.encode(passwordDto.getNewPassword()));
        userRepository.save(user);
    }
    // --- PHƯƠNG THỨC MỚI: CẬP NHẬT AVATAR ---
    @Transactional
    public String updateAvatar(User user, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("File upload không được để trống.");
        }

        // 1. Tạo tên file duy nhất để tránh trùng lặp
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID().toString() + fileExtension;

        // 2. Định nghĩa đường dẫn lưu file vật lý
        Path path = Paths.get(UPLOAD_DIR + newFilename);

        // 3. Lưu file
        Files.copy(file.getInputStream(), path);

        // 4. Cập nhật đường dẫn file cũ (Nếu cần xóa file cũ, bạn sẽ thêm logic ở đây)

        // 5. Lưu đường dẫn tương đối (dùng cho HTML) vào DB
        String avatarUrl = "/avatars/" + newFilename; // Đường dẫn tương đối từ static/
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);

        return avatarUrl;
    }
}
