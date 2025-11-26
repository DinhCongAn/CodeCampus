package com.codecampus.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Email là bắt buộc")
    @Email(message = "Email không đúng định dạng")
    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash; // Ánh xạ tới cột 'password_hash'

    @NotBlank(message = "Họ tên không được để trống")
    @Size(min = 2, max = 50, message = "Họ tên phải từ 2-50 ký tự")
    @Column(name = "full_name")
    private String fullName;

    // === CÁC THUỘC TÍNH BỔ SUNG TỪ DB SCRIPT ===

    @Column(length = 10)
    private String gender; // Cột 'gender' (NVARCHAR(10))

    @Pattern(regexp = "(84|0[3|5|7|8|9])+([0-9]{8})\\b", message = "Số điện thoại không hợp lệ (VN)")
    private String mobile;

    @Column(name = "avatar", columnDefinition = "NTEXT")
    private String avatarUrl; // Cột 'avatar' (NTEXT)

    @Column(name = "address", columnDefinition = "NTEXT")
    private String address; // Cột 'address' (NTEXT)

    // ===========================================

    @ManyToOne
    @JoinColumn(name = "role_id")
    private UserRole role;

    @Column(length = 50, columnDefinition = "NVARCHAR(50) DEFAULT 'pending'")
    private String status;

    @Column(name = "created_at", columnDefinition = "DATETIME DEFAULT GETDATE()")
    private LocalDateTime createdAt;

    // Bổ sung cột 'updated_at' từ DB script
    @Column(name = "updated_at", columnDefinition = "DATETIME DEFAULT GETDATE()")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now(); // Khởi tạo updated_at khi tạo
        if (status == null) {
            status = "pending"; // Đảm bảo trạng thái 'pending' như trong DB
        }
    }

    // Tự động cập nhật updated_at trước khi lưu
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}