// src/main/java/com/codecampus/service/LessonService.java
package com.codecampus.service;

import com.codecampus.entity.Lesson;
import com.codecampus.repository.LessonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LessonService {

    @Autowired
    private LessonRepository lessonRepository;

    /**
     * Lấy một bài học cụ thể bằng ID
     */
    @Transactional(readOnly = true)
    public Lesson getLessonById(Integer lessonId) {
        return lessonRepository.findById(Long.valueOf(lessonId))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài học"));
    }

    /**
     * Lấy toàn bộ danh sách bài học (đã sắp xếp) của một khóa học
     * (Dùng cho sidebar điều hướng)
     */
    @Transactional(readOnly = true)
    public List<Lesson> getLessonsByCourseId(Integer courseId) {
        return lessonRepository.findByCourseIdOrderByOrderNumberAsc(courseId);
    }

    // ===== BỔ SUNG PHƯƠNG THỨC NÀY =====
    /**
     * Lấy bài học đầu tiên của khóa học (để redirect)
     */
    @Transactional(readOnly = true)
    public Lesson getFirstLesson(Integer courseId) {
        return lessonRepository.findFirstByCourseIdOrderByOrderNumberAsc(courseId)
                .orElseThrow(() -> new RuntimeException("Khóa học này chưa có bài học."));
    }
}