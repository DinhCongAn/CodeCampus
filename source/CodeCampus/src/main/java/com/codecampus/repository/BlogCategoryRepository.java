package com.codecampus.repository;

import com.codecampus.entity.BlogCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository quản lý các danh mục bài viết (Blog Category).
 * Sử dụng Spring Data JPA để tự động hóa các câu lệnh SQL.
 */
@Repository
public interface BlogCategoryRepository extends JpaRepository<BlogCategory, Integer> {

    /**
     * Tìm danh sách các danh mục dựa trên trạng thái hoạt động.
     * Tương thích hoàn toàn với cả SQL Server (kiểu bit) và MySQL/TiDB (kiểu tinyint).
     *
     * @param isActive Trạng thái hoạt động (true/false)
     * @return Danh sách các danh mục thỏa mãn điều kiện
     */
    List<BlogCategory> findByIsActive(boolean isActive);
}