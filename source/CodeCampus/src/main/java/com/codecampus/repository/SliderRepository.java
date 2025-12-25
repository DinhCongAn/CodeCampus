package com.codecampus.repository;

import com.codecampus.entity.Slider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository quản lý các ảnh bìa quảng cáo (Slider) trên trang chủ.
 * Hỗ trợ các tính năng hiển thị cho người dùng và quản lý nâng cao cho Admin.
 * Thiết kế đảm bảo tương thích 100% với SQL Server và TiDB/MySQL.
 */
@Repository
public interface SliderRepository extends JpaRepository<Slider, Integer> {

    /**
     * Tìm danh sách các slider dựa trên trạng thái (Ví dụ: 'active').
     * Thường dùng để lấy danh sách hiển thị trên Carousel của trang chủ.
     * * @param status Trạng thái của slider
     * @return Danh sách các slider thỏa mãn điều kiện
     */
    List<Slider> findByStatus(String status);

    /**
     * Truy vấn tìm kiếm và lọc slider dành cho giao diện Quản trị (Admin).
     * Tương thích DB:
     * - Sử dụng LOWER và CONCAT chuẩn JPQL để tìm kiếm không phân biệt hoa thường.
     * - Loại bỏ hoàn toàn các lệnh COLLATE đặc thù của SQL Server.
     * - Tự động hỗ trợ phân trang (Pageable) cho mọi hệ quản trị cơ sở dữ liệu.
     * * @param keyword Từ khóa tìm kiếm theo tiêu đề hoặc đường dẫn liên kết
     * @param status Bộ lọc theo trạng thái
     * @param pageable Tham số phân trang và sắp xếp
     */
    @Query("SELECT s FROM Slider s " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.backlink) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:status IS NULL OR :status = '' OR s.status = :status) " +
            "ORDER BY s.id DESC")
    Page<Slider> findSlidersAdmin(@Param("keyword") String keyword,
                                  @Param("status") String status,
                                  Pageable pageable);

    /**
     * Tìm kiếm slider theo tiêu đề chính xác (không phân biệt hoa thường).
     * Dùng để kiểm tra trùng lặp dữ liệu.
     * * @param title Tiêu đề slider
     * @return Đối tượng Slider nếu tìm thấy
     */
    @Query("SELECT s FROM Slider s WHERE LOWER(s.title) = LOWER(:title)")
    Slider findByTitleIgnoreCase(@Param("title") String title);
}