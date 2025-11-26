package com.codecampus.service;

import com.codecampus.entity.CourseCategory;
import com.codecampus.repository.CourseCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {

    @Autowired private CourseCategoryRepository categoryRepository;

    // 1. Lấy danh sách
    public Page<CourseCategory> getCategoriesAdmin(String keyword, Boolean isActive, int page, int size) {
        return categoryRepository.findCategoriesAdmin(keyword, isActive, PageRequest.of(page, size));
    }

    // 2. Lấy chi tiết
    public CourseCategory getCategoryById(Integer id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục ID: " + id));
    }

    // 3. Lưu (Thêm/Sửa)
    public void saveCategory(CourseCategory category) {
        // 1. Cắt khoảng trắng thừa trước khi xử lý
        String cleanName = category.getName().trim();
        category.setName(cleanName);

        // 2. Check trùng tên (Dùng tên đã làm sạch)
        checkDuplicateName(category.getId(), cleanName);

        if (category.getId() == null) {
            if (category.getIsActive() == null) category.setIsActive(true);
        }

        categoryRepository.save(category);
    }

    // 4. Đổi trạng thái (Toggle)
    public void toggleStatus(Integer id) {
        CourseCategory cat = getCategoryById(id);
        // Đảo ngược trạng thái (true -> false, false -> true)
        cat.setIsActive(!Boolean.TRUE.equals(cat.getIsActive()));
        categoryRepository.save(cat);
    }

    // Hàm check trùng tên
    private void checkDuplicateName(Integer id, String name) {
        // Gọi hàm tìm kiếm KHÔNG phân biệt hoa thường mới viết trong Repo
        CourseCategory existing = categoryRepository.findByNameIgnoreCase(name);

        if (existing != null) {
            // Case 1: Thêm mới (id null) mà tìm thấy -> TRÙNG
            if (id == null) {
                throw new RuntimeException("Tên danh mục '" + name + "' đã tồn tại (Vui lòng kiểm tra lại).");
            }

            // Case 2: Sửa (id != null)
            // Tìm thấy tên đó, nhưng ID của thằng tìm thấy KHÁC ID thằng đang sửa
            // -> Nghĩa là đang lấy tên của thằng khác đặt cho mình -> TRÙNG
            if (!existing.getId().equals(id)) {
                throw new RuntimeException("Tên danh mục '" + name + "' đã được sử dụng.");
            }
        }
    }
}