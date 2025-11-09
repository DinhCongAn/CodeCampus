package com.codecampus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "price_packages")
public class PricePackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "course_id")
    private Integer courseId;
    private String name;
    @Column(name = "duration_months")
    private Integer durationMonths;
    @Column(name = "list_price")
    private BigDecimal listPrice; // (original price)
    @Column(name = "sale_price")
    private BigDecimal salePrice; // (sale price)
    private String status;
}