package com.codecampus.repository;

import com.codecampus.entity.Lesson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    /**
     * Tìm tất cả bài học thuộc một khóa học (courseId),
     * và sắp xếp chúng theo order_number (thứ tự bài học)
     */
    List<Lesson> findByCourseIdAndStatusOrderByOrderNumberAsc(Integer courseId, String status);

    // ===== BỔ SUNG PHƯƠNG THỨC NÀY =====
    /**
     * Tìm bài học ĐẦU TIÊN (có orderNumber nhỏ nhất) của một khóa học.
     */
    Optional<Lesson> findFirstByCourseIdAndStatusOrderByOrderNumberAsc(Integer courseId, String status);
    Optional<Lesson> findByLabId(Integer labId);
    Optional<Lesson> findByQuizId(Integer quizId);

    long countByCourseIdAndStatus(Integer courseId, String status);
    List<Lesson> findByCourseId(Integer courseId);

    // 1. Tìm bài học theo khóa
    @Query("SELECT l FROM Lesson l WHERE l.course.id = :courseId " +
            "AND (:status IS NULL OR :status = '' OR l.status = :status) " +
            "ORDER BY l.orderNumber ASC")
    Page<Lesson> findLessonsByCourse(@Param("courseId") Long courseId,
                                     @Param("status") String status,
                                     Pageable pageable);

    // 2. Tìm số thứ tự lớn nhất (để gợi ý số tiếp theo)
    @Query("SELECT COALESCE(MAX(l.orderNumber), 0) FROM Lesson l WHERE l.course.id = :courseId")
    Integer findMaxOrderNumber(@Param("courseId") Long courseId);

    // 3. Kiểm tra trùng
    @Query("SELECT COUNT(l) > 0 FROM Lesson l " +
            "WHERE l.course.id = :courseId " +
            "AND l.orderNumber = :orderNumber " +
            "AND (:lessonId IS NULL OR l.id != :lessonId)")
    boolean existsByOrderNumber(@Param("courseId") Long courseId,
                                @Param("orderNumber") Integer orderNumber,
                                @Param("lessonId") Long lessonId);

    // 4. [MỚI] TỰ ĐỘNG ĐẨY SỐ THỨ TỰ (Shift Down)
    // Logic: Tăng Order lên 1 cho tất cả các bài đứng sau vị trí chèn
    @Modifying // Bắt buộc vì là lệnh UPDATE
    @Query("UPDATE Lesson l SET l.orderNumber = l.orderNumber + 1 " +
            "WHERE l.course.id = :courseId AND l.orderNumber >= :orderNumber")
    void shiftOrdersDown(@Param("courseId") Long courseId,
                         @Param("orderNumber") Integer orderNumber);

    // [MỚI] Check trùng TÊN bài học trong cùng 1 khóa
    @Query("SELECT COUNT(l) > 0 FROM Lesson l " +
            "WHERE l.course.id = :courseId " +
            "AND LOWER(l.name) = LOWER(:name) " +
            "AND (:lessonId IS NULL OR l.id != :lessonId)")
    boolean existsByName(@Param("courseId") Long courseId,
                         @Param("name") String name,
                         @Param("lessonId") Long lessonId);

    @Query("SELECT l FROM Lesson l WHERE l.course.id = :courseId " +
            "AND (:keyword IS NULL OR :keyword = '' OR LOWER(l.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:typeId IS NULL OR l.lessonType.id = :typeId) " +
            "AND (:status IS NULL OR :status = '' OR l.status = :status) " +
            "ORDER BY l.orderNumber ASC")
    Page<Lesson> findLessonsByCourse(@Param("courseId") Long courseId,
                                     @Param("keyword") String keyword,
                                     @Param("typeId") Integer typeId,
                                     @Param("status") String status,
                                     Pageable pageable);
}
