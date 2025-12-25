package com.codecampus.repository;

import com.codecampus.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository quản lý các ghi chú cá nhân (Note) của người dùng trong bài học.
 * Đảm bảo tính tương thích đồng nhất giữa SQL Server (Local) và TiDB/MySQL (Production).
 */
@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    /**
     * Lấy danh sách ghi chú của một người dùng cụ thể trong một bài học cụ thể.
     * Tương thích DB: Hibernate tự động ánh xạ kiểu dữ liệu Integer/Long
     * sang định dạng phù hợp của từng hệ quản trị cơ sở dữ liệu.
     * * @param userId ID của người dùng (Integer)
     * @param lessonId ID của bài học (Long)
     * @return Danh sách các ghi chú tìm thấy
     */
    List<Note> findByUserIdAndLessonId(Integer userId, Long lessonId);

    /**
     * Tìm kiếm một ghi chú cụ thể bằng ID và kiểm tra quyền sở hữu (userId).
     * Đây là phương thức quan trọng để đảm bảo tính bảo mật dữ liệu:
     * Người dùng chỉ có thể truy cập, sửa hoặc xóa ghi chú do chính họ tạo ra.
     * * @param id ID của ghi chú (Long)
     * @param userId ID của người dùng sở hữu (Integer)
     * @return Đối tượng ghi chú bọc trong Optional
     */
    Optional<Note> findByIdAndUserId(Long id, Integer userId);
}