package com.codecampus.service;

import com.codecampus.entity.Setting;
import com.codecampus.repository.SettingRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class SettingService {

    @Autowired
    private SettingRepository settingRepository;

    public Page<Setting> getAllSettings(String keyword, String type, String status, Pageable pageable) {
        Specification<Setting> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Tìm kiếm (SỬA LẠI ĐOẠN NÀY)
            if (StringUtils.hasText(keyword)) {
                // Không cần toLowerCase() nữa
                String likeKey = "%" + keyword + "%";

                predicates.add(cb.or(
                        cb.like(root.get("settingKey"), likeKey),
                        cb.like(root.get("settingValue"), likeKey), // Lưu ý: Dùng tên trường trong Entity (camelCase)
                        cb.like(root.get("description"), likeKey)
                ));
            }

            // 2. Lọc theo Type
            if (StringUtils.hasText(type)) {
                predicates.add(cb.equal(root.get("type"), type));
            }

            // 3. Lọc theo Status
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            } else {
                // Mặc định ẩn những cái đã xóa mềm
                predicates.add(cb.notEqual(root.get("status"), "deleted"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return settingRepository.findAll(spec, pageable);
    }

    public List<String> getAllTypes() {
        return settingRepository.findDistinctActiveTypes();
    }

    public Setting getSettingById(Integer id) {
        return settingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Setting not found with id: " + id));
    }

    public void saveSetting(Setting setting) throws Exception {
        // 1. Kiểm tra trùng Key
        if (setting.getId() == null) {
            // Trường hợp Thêm mới
            if (settingRepository.existsBySettingKey(setting.getSettingKey())) {
                throw new Exception("Key '" + setting.getSettingKey() + "' đã tồn tại!");
            }
        } else {
            // Trường hợp Update
            if (settingRepository.existsBySettingKeyAndIdNot(setting.getSettingKey(), setting.getId())) {
                throw new Exception("Key '" + setting.getSettingKey() + "' đã được sử dụng bởi setting khác!");
            }
        }

        // 2. Set mặc định nếu thiếu
        if (setting.getStatus() == null || setting.getStatus().isEmpty()) {
            setting.setStatus("active");
        }

        // 3. Lưu
        settingRepository.save(setting);
    }

    public Setting getById(Integer id) {
        return settingRepository.findById(id).orElse(null);
    }

    public void toggleStatus(Integer id) throws Exception {
        Setting setting = settingRepository.findById(id)
                .orElseThrow(() -> new Exception("Không tìm thấy ID: " + id));

        // Đảo ngược trạng thái: active <-> inactive
        if ("active".equalsIgnoreCase(setting.getStatus())) {
            setting.setStatus("inactive");
        } else {
            setting.setStatus("active");
        }
        settingRepository.save(setting);
    }

    public String getSettingValue(String key) {
        // Chỉ lấy setting nếu nó đang "active"
        return settingRepository.findBySettingKeyAndStatus(key, "active")
                .map(Setting::getSettingValue)
                .orElse(null); // Trả về null hoặc "" nếu không tìm thấy hoặc đang inactive
    }

    // Trong SettingService.java

    // Cách xóa mềm (An toàn hơn)
    public void deleteSetting(Integer id) throws Exception {
        Setting setting = settingRepository.findById(id)
                .orElseThrow(() -> new Exception("Không tìm thấy ID: " + id));
        settingRepository.delete(setting);
    }
}