package com.codecampus.entity;

import jakarta.persistence.*;
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

    @Column(length = 255, nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash; // Ánh xạ tới cột 'password_hash'

    @Column(name = "full_name", length = 255)
    private String fullName;

    // === CÁC THUỘC TÍNH BỔ SUNG TỪ DB SCRIPT ===

    @Column(length = 10)
    private String gender; // Cột 'gender' (NVARCHAR(10))

    @Column(length = 20)
    private String mobile; // Cột 'mobile' (VARCHAR(20))

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