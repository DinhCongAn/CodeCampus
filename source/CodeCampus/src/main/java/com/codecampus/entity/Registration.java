// src/main/java/com/codecampus/entity/Registration.java
package com.codecampus.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "registrations")
@Getter
@Setter
@NoArgsConstructor
public class Registration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne @JoinColumn(name = "course_id")
    private Course course;
    @ManyToOne @JoinColumn(name = "package_id")
    private PricePackage pricePackage;
    @Column(name = "order_code", unique = true, nullable = false)
    private String orderCode;
    @Column(name = "registration_time", nullable = false)
    private LocalDateTime registrationTime;
    @Column(name = "total_cost", nullable = false)
    private BigDecimal totalCost;
    public String status;
    @Column(name = "valid_from")
    private LocalDateTime validFrom;
    @Column(name = "valid_to")
    private LocalDateTime validTo;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}