package com.codecampus.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedback_attachments")
@Data // Tự sinh Getter, Setter, toString, equals, hashCode
@NoArgsConstructor // Constructor rỗng cho JPA
@AllArgsConstructor // Constructor full tham số
@Builder // Pattern Builder để tạo object dễ dàng
public class FeedbackAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID tự tăng (Bắt buộc với SQL Server)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE) // Xóa Feedback thì xóa luôn file đính kèm
    @JoinColumn(name = "feedback_id", nullable = false)
    private Feedback feedback;

    @Nationalized // Hỗ trợ tên file tiếng Việt
    @Column(name = "file_name", nullable = false)
    @NotBlank(message = "Tên file không được để trống")
    @Size(max = 255, message = "Tên file quá dài")
    private String fileName;

    @Nationalized
    @Column(name = "file_url", nullable = false, length = 512)
    @NotBlank(message = "Đường dẫn file không được để trống")
    @Size(max = 512, message = "URL quá dài")
    private String fileUrl;

    @Nationalized
    @Column(name = "file_type", nullable = false, length = 50)
    @NotBlank(message = "Loại file không được để trống")
    private String fileType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // --- Lifecycle Hooks ---
    // Tự động set thời gian khi lưu vào DB
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}