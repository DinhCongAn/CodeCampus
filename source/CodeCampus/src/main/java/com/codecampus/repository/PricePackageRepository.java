package com.codecampus.repository;

import com.codecampus.entity.PricePackage;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository quản lý các gói giá (Price Package) của khóa học.
 * Thiết kế đảm bảo tính tương thích đa cơ sở dữ liệu (Database Agnostic).
 */
@Repository
public interface PricePackageRepository extends JpaRepository<PricePackage, Integer> {

    /**
     * Truy vấn nội bộ để tìm gói giá dựa trên logic ưu tiên giá Sale.
     * Tương thích: Sử dụng JPQL và Pageable thay cho TOP/LIMIT thủ công.
     * COALESCE(p.salePrice, p.listPrice): Lấy giá sale nếu có, nếu không thì lấy giá gốc.
     * * @param courseId ID của khóa học
     * @param pageable Đối tượng phân trang (dùng để giới hạn lấy 1 bản ghi)
     */
    @Query("SELECT p FROM PricePackage p " +
            "WHERE p.course.id = :courseId AND p.status = 'active' " +
            "ORDER BY COALESCE(p.salePrice, p.listPrice) ASC")
    List<PricePackage> findLowestPricePackageInternal(@Param("courseId") Integer courseId, Pageable pageable);

    /**
     * Tìm gói giá thấp nhất của một khóa học.
     * Đây là hàm Wrapper giúp giữ nguyên chữ ký hàm (Method Signature) cho tầng Service.
     * * @param courseId ID của khóa học
     * @return Optional chứa gói giá rẻ nhất nếu tìm thấy
     */
    default Optional<PricePackage> findLowestPricePackageByCourseId(Integer courseId) {
        // Hibernate sẽ tự dịch PageRequest.of(0, 1) thành TOP 1 (SQL Server) hoặc LIMIT 1 (MySQL/TiDB)
        List<PricePackage> result = findLowestPricePackageInternal(courseId, PageRequest.of(0, 1));
        return result.stream().findFirst();
    }

    /**
     * Lấy tất cả các gói giá thuộc về một khóa học cụ thể.
     */
    List<PricePackage> findByCourseId(Integer courseId);

    /**
     * Kiểm tra sự tồn tại của tên gói giá trong cùng một khóa học (không phân biệt hoa thường).
     * Dùng để tránh trùng tên khi tạo gói giá mới cho một môn học.
     * * @param courseId ID khóa học
     * @param name Tên gói giá cần check
     * @param id ID của gói giá hiện tại (dùng khi Update, bỏ qua chính nó)
     */
    @Query("SELECT COUNT(p) > 0 FROM PricePackage p " +
            "WHERE p.course.id = :courseId " +
            "AND LOWER(p.name) = LOWER(:name) " +
            "AND (:id IS NULL OR p.id != :id)")
    boolean existsByNameAndCourse(@Param("courseId") Integer courseId,
                                  @Param("name") String name,
                                  @Param("id") Integer id);
}