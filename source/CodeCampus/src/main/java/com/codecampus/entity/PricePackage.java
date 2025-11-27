//package com.codecampus.entity;
//
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.Setter;
//import java.math.BigDecimal;
//
//@Getter
//@Setter
//@Entity
//@Table(name = "price_packages")
//public class PricePackage {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Integer id;
//    @Column(name = "course_id")
//    private Integer courseId;
//    private String name;
//    @Column(name = "duration_months")
//    private Integer durationMonths;
//    @Column(name = "list_price")
//    private BigDecimal listPrice; // (original price)
//    @Column(name = "sale_price")
//    private BigDecimal salePrice; // (sale price)
//    private String status;
//}
package com.codecampus.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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

    @NotBlank(message = "Tên gói không được để trống")
    @Size(max = 100)
    private String name;

    @NotNull(message = "Vui lòng nhập thời gian truy cập")
    @Min(value = 1, message = "Thời gian phải ít nhất 1 tháng")
    @Column(name = "duration_months")
    private Integer durationMonths;

    @NotNull(message = "Vui lòng nhập giá niêm yết")
    @Min(value = 0, message = "Giá không được âm")
    @Column(name = "list_price")
    private BigDecimal listPrice;

    @Column(name = "sale_price")
    private BigDecimal salePrice; // Có thể null hoặc 0

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    private String status; // 'active', 'inactive'

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    // Getter xử lý logic hiển thị (Tùy chọn)
    public BigDecimal getSalePrice() {
        return salePrice != null ? salePrice : listPrice;
    }
}