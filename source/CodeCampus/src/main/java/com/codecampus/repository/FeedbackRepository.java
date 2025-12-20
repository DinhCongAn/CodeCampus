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

    // Lấy top feedback có rating >= 4 sao, sắp xếp mới nhất
    // Sử dụng JOIN FETCH để lấy luôn thông tin User và Course (tối ưu hiệu năng)
    @Query("SELECT f FROM Feedback f " +
            "JOIN FETCH f.user u " +
            "JOIN FETCH f.course c " +
            "WHERE f.rating >= 4 " +
            "ORDER BY f.createdAt DESC")
    List<Feedback> findTopFeedbacks(Pageable pageable);


    // Tìm feedback của user cụ thể trong 1 khóa học (để check đã review chưa)
    Optional<Feedback> findByUserIdAndCourseId(Integer userId, Integer courseId);

    // Lấy danh sách feedback của course (để hiển thị)
    Page<Feedback> findByCourseIdOrderByCreatedAtDesc(Integer courseId, Pageable pageable);

    // Tính điểm trung bình và tổng số review
    // Trả về Object[] gồm [Double avg, Long count]
    @Query("SELECT COALESCE(AVG(CAST(f.rating AS double)), 0.0), COUNT(f) " +
            "FROM Feedback f WHERE f.course.id = :courseId")
    List<Object[]> findAverageRatingAndCountByCourseId(@Param("courseId") Integer courseId);

    @Query("SELECT f FROM Feedback f WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            " f.user.fullName LIKE :keyword OR " +
            " f.course.name LIKE :keyword OR " +
            " f.comment LIKE :keyword) " +
            "AND (:rating IS NULL OR f.rating = :rating) " +      // [MỚI] Lọc theo sao
            "AND (:courseId IS NULL OR f.course.id = :courseId) " + // [MỚI] Lọc theo khóa học
            "ORDER BY f.createdAt DESC")
    Page<Feedback> searchFeedbacks(@Param("keyword") String keyword,
                                   @Param("rating") Integer rating,
                                   @Param("courseId") Integer courseId,
                                   Pageable pageable);
}

