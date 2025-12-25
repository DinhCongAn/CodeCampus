package com.codecampus.repository;

import com.codecampus.entity.PricePackage;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PricePackageRepository extends JpaRepository<PricePackage, Integer> {

    // --- PHẦN SỬA ĐỔI: Dùng JPQL chuẩn thay vì Native Query ---

    // 1. Tạo hàm truy vấn nội bộ dùng JPQL (Làm việc với Entity, không phải Table)
    // Lưu ý: Trong JPQL phải dùng tên biến trong Java (ví dụ: salePrice) chứ không phải tên cột SQL (sale_price)
    @Query("SELECT p FROM PricePackage p " +
            "WHERE p.course.id = :courseId AND p.status = 'active' " +
            "ORDER BY COALESCE(p.salePrice, p.listPrice) ASC")
    List<PricePackage> findLowestPricePackageInternal(@Param("courseId") Integer courseId, Pageable pageable);

    // 2. Tạo hàm default (Java 8+) để giữ nguyên tên hàm cũ -> Service không cần sửa code
    // Hàm này sẽ tự động gọi hàm trên và lấy 1 dòng đầu tiên (PageRequest.of(0, 1))
    default Optional<PricePackage> findLowestPricePackageByCourseId(Integer courseId) {
        List<PricePackage> result = findLowestPricePackageInternal(courseId, PageRequest.of(0, 1));
        return result.stream().findFirst();
    }

    // -----------------------------------------------------------

    // Các hàm khác giữ nguyên hoặc chuẩn hóa lại
    List<PricePackage> findByCourseId(Integer courseId);

    // Lưu ý: getPackagesByCourseId và findByCourseId chức năng y hệt nhau, nên xóa bớt 1 cái nếu không dùng
    // Nếu CourseId trong DB là Integer thì xóa dòng Long đi, hoặc ngược lại.
    // Tôi giữ lại để bạn không bị lỗi biên dịch nếu lỡ có chỗ đang gọi.

    @Query("SELECT COUNT(p) > 0 FROM PricePackage p " +
            "WHERE p.course.id = :courseId " +
            "AND LOWER(p.name) = LOWER(:name) " +
            "AND (:id IS NULL OR p.id != :id)")
    boolean existsByNameAndCourse(@Param("courseId") Integer courseId,
                                  @Param("name") String name,
                                  @Param("id") Integer id);
}