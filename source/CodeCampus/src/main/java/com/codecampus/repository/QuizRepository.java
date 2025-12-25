package com.codecampus.repository;

import com.codecampus.entity.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository quản lý các bài kiểm tra (Quiz).
 * Hỗ trợ các chức năng tìm kiếm nâng cao, thống kê câu hỏi và kiểm tra ràng buộc dữ liệu.
 * Đảm bảo hoạt động đồng nhất trên cả SQL Server và MySQL/TiDB.
 */
@Repository
public interface QuizRepository extends JpaRepository<Quiz, Integer> {

    /**
     * Truy vấn tìm kiếm và lọc bài kiểm tra dành cho giao diện Quản trị (Admin).
     * Tương thích DB: Sử dụng LOWER và CONCAT để tìm kiếm không phân biệt hoa thường.
     * * @param keyword Từ khóa tìm kiếm theo tên bài kiểm tra
     * @param courseId ID của khóa học liên kết
     * @param typeId ID loại bài kiểm tra (Test Type)
     * @param pageable Tham số phân trang và sắp xếp
     */
    @Query("SELECT q FROM Quiz q " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR LOWER(q.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:courseId IS NULL OR q.course.id = :courseId) " +
            "AND (:typeId IS NULL OR q.testType.id = :typeId) " +
            "ORDER BY q.id DESC")
    Page<Quiz> findQuizzesAdmin(@Param("keyword") String keyword,
                                @Param("courseId") Integer courseId,
                                @Param("typeId") Integer typeId,
                                Pageable pageable);

    /**
     * Đếm tổng số lượng câu hỏi có trong một bài kiểm tra cụ thể.
     * Sử dụng JPQL Join để đếm chính xác số lượng thực thể liên kết.
     * * @param quizId ID của bài kiểm tra
     * @return Số lượng câu hỏi
     */
    @Query("SELECT COUNT(q) FROM Quiz quiz JOIN quiz.questions q WHERE quiz.id = :quizId")
    Integer countQuestionsByQuizId(@Param("quizId") Integer quizId);

    /**
     * Kiểm tra xem bài kiểm tra này đã từng có người thực hiện làm bài chưa.
     * Thường dùng để ngăn chặn việc xóa hoặc sửa các Quiz đã có dữ liệu lịch sử bài làm.
     * * @param quizId ID của bài kiểm tra
     * @return true nếu đã có ít nhất một lượt làm bài (Attempt)
     */
    @Query("SELECT COUNT(qa) > 0 FROM QuizAttempt qa WHERE qa.quiz.id = :quizId")
    boolean hasAttempts(@Param("quizId") Integer quizId);

    /**
     * Kiểm tra sự tồn tại của tên bài kiểm tra trong phạm vi một khóa học (không phân biệt hoa thường).
     * Dùng để tránh trùng lặp dữ liệu khi tạo mới hoặc cập nhật.
     * * @param name Tên bài kiểm tra cần kiểm tra
     * @param courseId ID của khóa học
     * @param quizId ID của Quiz hiện tại (bỏ qua chính nó khi thực hiện lệnh Update)
     */
    @Query("SELECT COUNT(q) > 0 FROM Quiz q " +
            "WHERE q.course.id = :courseId " +
            "AND LOWER(q.name) = LOWER(:name) " +
            "AND (:quizId IS NULL OR q.id != :quizId)")
    boolean existsByNameAndCourse(@Param("name") String name,
                                  @Param("courseId") Integer courseId,
                                  @Param("quizId") Integer quizId);

    /**
     * Tìm danh sách các bài kiểm tra thuộc về một khóa học cụ thể.
     * Giữ nguyên kiểu dữ liệu Long theo yêu cầu cấu trúc thực thể Course của bạn.
     */
    List<Quiz> findByCourseId(Long courseId);

}