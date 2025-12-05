package com.codecampus.repository;

import com.codecampus.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // Để hỗ trợ tìm kiếm nâng cao
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional; // Quan trọng: Import thư viện này

@Repository
public interface SettingRepository extends JpaRepository<Setting, Integer>, JpaSpecificationExecutor<Setting> {

    // --- Thêm dòng này ---
    // Spring Data JPA sẽ tự động sinh câu lệnh SQL: SELECT * FROM settings WHERE key_name = ?
    Optional<Setting> findBySettingKeyAndStatus(String settingKey, String status);
    // Lưu ý:
    // Nếu trong Entity bạn đặt tên biến là 'key' thì sửa thành: findByKey(String key)
    // Nếu trong Entity bạn đặt tên biến là 'name' thì sửa thành: findByName(String name)

    // Hàm lấy danh sách type (bạn đã có)
    @Query("SELECT DISTINCT s.type FROM Setting s WHERE s.status = 'active'")
    List<String> findDistinctActiveTypes();

    boolean existsBySettingKey(String settingKey);
    boolean existsBySettingKeyAndIdNot(String settingKey, Integer id); // Dùng khi update (trùng key nhưng khác ID)
}