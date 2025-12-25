package com.codecampus.repository;

import com.codecampus.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository quản lý các cấu hình hệ thống (Settings/Configurations).
 * Hỗ trợ tìm kiếm linh hoạt thông qua JpaSpecificationExecutor và các truy vấn chuẩn.
 * Đảm bảo hoạt động ổn định trên cả SQL Server (Local) và TiDB/MySQL (Render).
 */
@Repository
public interface SettingRepository extends JpaRepository<Setting, Integer>, JpaSpecificationExecutor<Setting> {

    /**
     * Tìm kiếm cấu hình theo khóa (Key) và trạng thái.
     * Tương thích DB: Spring Data JPA tự động sinh câu lệnh SELECT chuẩn.
     * * @param settingKey Khóa định danh của cấu hình (Ví dụ: 'SITE_LOGO', 'CONTACT_EMAIL')
     * @param status Trạng thái (Ví dụ: 'active', 'inactive')
     * @return Optional chứa đối tượng Setting nếu tìm thấy
     */
    Optional<Setting> findBySettingKeyAndStatus(String settingKey, String status);

    /**
     * Lấy danh sách các loại cấu hình (Type) đang hoạt động một cách duy nhất.
     * Thường dùng để phân loại cấu hình trong giao diện Admin (ví dụ: System, Mail, Social).
     * * @return Danh sách các chuỗi định danh loại cấu hình
     */
    @Query("SELECT DISTINCT s.type FROM Setting s WHERE s.status = 'active'")
    List<String> findDistinctActiveTypes();

    /**
     * Kiểm tra sự tồn tại của một Setting Key.
     * Dùng khi tạo mới cấu hình để đảm bảo Key là duy nhất.
     */
    boolean existsBySettingKey(String settingKey);

    /**
     * Kiểm tra sự tồn tại của Setting Key trừ một ID cụ thể.
     * Dùng trong logic cập nhật (Update) để kiểm tra Key mới không bị trùng với các bản ghi khác.
     * * @param settingKey Khóa cần kiểm tra
     * @param id ID của bản ghi hiện tại cần bỏ qua
     */
    boolean existsBySettingKeyAndIdNot(String settingKey, Integer id);
}