package com.codecampus.repository;

import com.codecampus.entity.Slider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SliderRepository extends JpaRepository<Slider, Integer> {
    // Tìm các slider theo trạng thái
    List<Slider> findByStatus(String status);

    @Query("SELECT s FROM Slider s " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.backlink) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:status IS NULL OR :status = '' OR s.status = :status) " +
            "ORDER BY s.id DESC") // Mới nhất lên đầu
    Page<Slider> findSlidersAdmin(@Param("keyword") String keyword,
                                  @Param("status") String status,
                                  Pageable pageable);

    @Query("SELECT s FROM Slider s WHERE LOWER(s.title) = LOWER(:title)")
    Slider findByTitleIgnoreCase(@Param("title") String title);
}
