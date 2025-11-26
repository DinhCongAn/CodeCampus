package com.codecampus.service;

import com.codecampus.entity.Course;
import com.codecampus.entity.CourseCategory;
import com.codecampus.entity.PricePackage;
import com.codecampus.repository.CourseCategoryRepository;
import com.codecampus.repository.CourseRepository;
import com.codecampus.repository.PricePackageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseCategoryRepository categoryRepository;
    private final PricePackageRepository priceRepository;

    public CourseService(CourseRepository courseRepository,
                         CourseCategoryRepository categoryRepository,
                         PricePackageRepository priceRepository) {
        this.courseRepository = courseRepository;
        this.categoryRepository = categoryRepository;
        this.priceRepository = priceRepository;
    }

    private static final String STATUS_PUBLISHED = "published";

    public Page<Course> getPublishedCourses(int page, int size, String keyword, Integer categoryId) {
        Pageable pageable = PageRequest.of(page, size);
        boolean hasKeyword = (keyword != null && !keyword.trim().isEmpty());
        boolean hasCategory = (categoryId != null && categoryId > 0);

        if (hasKeyword && hasCategory) {
            return courseRepository.findByStatusAndNameContainingIgnoreCaseAndCategoryIdOrderByUpdatedAtDesc(STATUS_PUBLISHED, keyword, categoryId, pageable);
        } else if (hasKeyword) {
            return courseRepository.findByStatusAndNameContainingIgnoreCaseOrderByUpdatedAtDesc(STATUS_PUBLISHED, keyword, pageable);
        } else if (hasCategory) {
            return courseRepository.findByStatusAndCategoryIdOrderByUpdatedAtDesc(STATUS_PUBLISHED, categoryId, pageable);
        } else {
            return courseRepository.findByStatusOrderByUpdatedAtDesc(STATUS_PUBLISHED, pageable);
        }
    }

    public Course getPublishedCourseById(Integer id) {
        return courseRepository.findByIdAndStatus(id, STATUS_PUBLISHED)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học hoặc chưa được xuất bản."));
    }

    public Optional<PricePackage> getLowestPrice(Integer courseId) {
        return priceRepository.findLowestPricePackageByCourseId(courseId);
    }

    public List<Course> getFeaturedCourses() {
        return courseRepository.findTop5ByStatusAndIsFeaturedOrderByUpdatedAtDesc(STATUS_PUBLISHED, true);
    }

    public List<CourseCategory> getAllActiveCategories() {

        return categoryRepository.findByIsActive(true);
    }

    public Course findCourseById(Integer id) {
        return courseRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học với ID: " + id));
    }

    // 1. Lấy danh sách (Không ảnh hưởng bởi ID)
    public Page<Course> getCoursesForAdmin(String keyword, Integer categoryId, String status, int page, int size) {
        return courseRepository.findCoursesAdmin(keyword, categoryId, status, PageRequest.of(page, size));
    }

    // 2. Lấy chi tiết
    // Controller truyền xuống Long, Repo nhận Long -> OK, giữ nguyên
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy môn học với ID: " + id));
    }

    // 3. Lưu môn học (Thêm mới hoặc Edit)
    // --- ĐÂY LÀ CHỖ ĐÃ SỬA LỖI NULL POINTER ---
    public void saveCourse(Course course) {
        // Truyền thẳng Integer id vào hàm check, không ép kiểu
        checkDuplicateName(course.getId(), course.getName());

        // Kiểm tra null an toàn cho ID
        if (course.getId() == null) {
            course.setCreatedAt(LocalDateTime.now());
            if (course.getIsFeatured() == null) {
                course.setIsFeatured(false);
            }
        }
        course.setUpdatedAt(LocalDateTime.now());

        courseRepository.save(course);
    }

    /**
     * Logic kiểm tra trùng tên (Đã sửa để chấp nhận Integer ID và xử lý NULL)
     */
    private void checkDuplicateName(Integer id, String name) {
        // Tìm xem có thằng nào tên giống vậy không
        Course existingCourse = courseRepository.findByName(name);

        if (existingCourse != null) {
            // TRƯỜNG HỢP 1: Thêm mới (id truyền vào là NULL)
            // Tìm thấy tên trong DB -> CHẮC CHẮN TRÙNG (vì chưa có ID để so sánh)
            if (id == null) {
                throw new RuntimeException("Tên môn học '" + name + "' đã tồn tại. Vui lòng chọn tên khác.");
            }

            // TRƯỜNG HỢP 2: Cập nhật (id có giá trị)
            // So sánh ID của thằng tìm thấy với ID thằng đang sửa.
            // QUAN TRỌNG: Dùng .equals() để so sánh đối tượng Integer an toàn.
            // Tuyệt đối không dùng != hoặc ép kiểu sang long/int ở đây.
            if (!existingCourse.getId().equals(id)) {
                throw new RuntimeException("Tên môn học '" + name + "' đã được sử dụng bởi khóa học khác.");
            }
        }
    }

    // 4. Đổi trạng thái
    public void toggleCourseStatus(Long id) {
        Course course = getCourseById(id);
        if ("ACTIVE".equals(course.getStatus())) {
            course.setStatus("INACTIVE");
        } else {
            course.setStatus("ACTIVE");
        }
        courseRepository.save(course);
    }
}