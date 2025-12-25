package com.codecampus.repository;

import com.codecampus.entity.QuestionLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository quản lý các cấp độ khó của câu hỏi (Question Level).
 * Ví dụ: Dễ, Trung bình, Khó, Chuyên gia.
 * Thiết kế đảm bảo hoạt động đồng nhất trên cả SQL Server và MySQL/TiDB.
 */
@Repository
public interface QuestionLevelRepository extends JpaRepository<QuestionLevel, Integer> {

    /**
     * Tìm kiếm cấp độ câu hỏi dựa trên tên định danh.
     * Tương thích DB: Hibernate tự động thực hiện truy vấn SELECT chuẩn
     * mà không phụ thuộc vào cú pháp riêng biệt của từng hệ quản trị.
     * * @param name Tên cấp độ (Ví dụ: 'Easy', 'Hard')
     * @return Đối tượng cấp độ bọc trong Optional để tránh lỗi NullPointerException
     */
    Optional<QuestionLevel> findByName(String name);
}