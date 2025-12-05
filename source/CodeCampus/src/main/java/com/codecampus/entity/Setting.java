package com.codecampus.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "settings")
@Data // Tự động sinh Getter, Setter, toString, equals, hashCode
@NoArgsConstructor // Constructor không tham số (Bắt buộc cho JPA)
@AllArgsConstructor // Constructor full tham số
@Builder // Giúp tạo đối tượng nhanh: Setting.builder().key("...").build()
public class Setting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // QUAN TRỌNG: Để SQL Server tự tăng ID
    @Column(name = "id", nullable = false)
    private Integer id;

    @Nationalized // Hỗ trợ lưu tiếng Việt (NVARCHAR)
    @Column(name = "type", length = 100)
    private String type;

    // Dùng escape \"value\" vì 'value' là từ khóa trong một số DB, giữ nguyên để an toàn
    @Nationalized
    @Column(name = "\"value\"", length = 255)
    private String value;

    @Column(name = "order_num")
    private Integer orderNum;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "setting_key", length = 100, unique = true) // Nên để unique để tránh trùng Key
    private String settingKey;

    @Nationalized
    @Lob // Báo hiệu đây là dữ liệu lớn (Large Object)
    @Column(name = "setting_value", columnDefinition = "NVARCHAR(MAX)") // Định nghĩa rõ cho SQL Server
    private String settingValue;

    @Nationalized
    @Lob
    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;
}