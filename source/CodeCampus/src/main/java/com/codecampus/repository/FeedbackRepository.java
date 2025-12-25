package com.codecampus.repository;

import com.codecampus.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {

    // JPQL chuẩn: JOIN FETCH hoạt động tốt trên cả 2 DB
    @Query("SELECT f FROM Feedback f " +
            "JOIN FETCH f.user u " +
            "JOIN FETCH f.course c " +
            "WHERE f.rating >= 4 " +
            "ORDER BY f.createdAt DESC")
    List<Feedback> findTopFeedbacks(Pageable pageable);


    // Method naming convention: Spring tự generate SQL, tương thích 100%
    Optional<Feedback> findByUserIdAndCourseId(Integer userId, Integer courseId);

    // Method naming convention: Tương thích 100%
    Page<Feedback> findByCourseIdOrderByCreatedAtDesc(Integer courseId, Pageable pageable);

    // [QUAN TRỌNG] Đã sửa: Thay CAST(... as double) bằng phép nhân (* 1.0)
    // Lý do: SQL Server dùng FLOAT, MySQL dùng DOUBLE.
    // Nhân với 1.0 là thủ thuật "Universal SQL" để ép kiểu về số thực trên mọi DB.
    // Kết quả vẫn trả về Double như yêu cầu.
    @Query("SELECT COALESCE(AVG(f.rating * 1.0), 0.0), COUNT(f) " +
            "FROM Feedback f WHERE f.course.id = :courseId")
    List<Object[]> findAverageRatingAndCountByCourseId(@Param("courseId") Integer courseId);

    // JPQL chuẩn: Logic tìm kiếm giữ nguyên
    // Lưu ý: Param keyword cần được xử lý thêm '%' ở tầng Service (ví dụ: "%abc%")
    // SQL Server và TiDB đều xử lý tốt logic (:param IS NULL OR ...)
    @Query("SELECT f FROM Feedback f WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            " f.user.fullName LIKE :keyword OR " +
            " f.course.name LIKE :keyword OR " +
            " f.comment LIKE :keyword) " +
            "AND (:rating IS NULL OR f.rating = :rating) " +
            "AND (:courseId IS NULL OR f.course.id = :courseId) " +
            "ORDER BY f.createdAt DESC")
    Page<Feedback> searchFeedbacks(@Param("keyword") String keyword,
                                   @Param("rating") Integer rating,
                                   @Param("courseId") Integer courseId,
                                   Pageable pageable);
}