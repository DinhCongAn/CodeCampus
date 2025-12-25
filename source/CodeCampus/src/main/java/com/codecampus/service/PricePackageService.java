package com.codecampus.service;

import com.codecampus.entity.Course;
import com.codecampus.entity.PricePackage;
import com.codecampus.repository.CourseRepository;
import com.codecampus.repository.PricePackageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PricePackageService {

    @Autowired private PricePackageRepository pricePackageRepository;
    @Autowired private CourseRepository courseRepository; // Để lấy thông tin khóa học

    public List<PricePackage> getPackagesByCourse(Long courseId) {
        return pricePackageRepository.findByCourseId(Math.toIntExact(courseId));
    }

    public PricePackage getPackageById(Integer id) {
        return pricePackageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy gói giá ID: " + id));
    }

    private void checkDuplicateName(Integer id, String name, Integer courseId) {
        // Kiểm tra trùng tên với các gói khác trong khóa học này
        boolean isDuplicate = pricePackageRepository.existsByNameAndCourse(courseId, name, id);
        if (isDuplicate) {
            // Nếu tìm thấy -> Báo lỗi ngay
            throw new RuntimeException("Tên gói '" + name + "' đã tồn tại trong khóa học này.");
        }
    }
    public void savePackage(PricePackage pkg, Long courseId) {

        // 1. Xử lý tên
        String cleanName = pkg.getName().trim();
        pkg.setName(cleanName);

        // Gọi hàm check (courseId.intValue() vì DB lưu Integer)
        checkDuplicateName(pkg.getId(), cleanName, courseId.intValue());


        if (pkg.getSalePrice() != null && pkg.getSalePrice().compareTo(pkg.getListPrice()) > 0) {
            throw new RuntimeException("Giá bán phải nhỏ hơn hoặc bằng giá niêm yết.");
        }

        // 2. Gắn Course vào Package (Nếu thêm mới)
        if (pkg.getId() == null) {
            // Lưu ý: Sửa kiểu Long/Integer cho khớp với Course Service của bạn
            Course course = courseRepository.findById((long) courseId.intValue())
                    .orElseThrow(() -> new RuntimeException("Khóa học không tồn tại"));
            pkg.setCourse(course);

            if (pkg.getStatus() == null) pkg.setStatus("active");
        } else {
            // Nếu update, giữ nguyên course cũ (an toàn)
            PricePackage old = getPackageById(pkg.getId());
            pkg.setCourse(old.getCourse());
        }

        pricePackageRepository.save(pkg);
    }

    public void deletePackage(Integer id) {
        pricePackageRepository.deleteById(id);
    }
}