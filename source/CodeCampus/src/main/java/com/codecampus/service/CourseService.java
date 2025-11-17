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
}