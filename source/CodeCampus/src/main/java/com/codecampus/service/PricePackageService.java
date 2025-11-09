// src/main/java/com/codecampus/service/PricePackageService.java
package com.codecampus.service;

import com.codecampus.entity.PricePackage;
import java.util.List;

/**
 * Interface cho các dịch vụ liên quan đến Gói giá (PricePackage).
 */
public interface PricePackageService {

    /**
     * Lấy tất cả các gói giá (PricePackage) dựa trên ID của khóa học (Course).
     */
    List<PricePackage> getPackagesByCourseId(Integer courseId);
}