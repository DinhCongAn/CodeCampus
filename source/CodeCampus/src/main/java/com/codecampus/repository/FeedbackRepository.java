package com.codecampus.repository;

import com.codecampus.entity.Feedback;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}