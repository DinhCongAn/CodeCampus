// src/main/java/com/codecampus/service/impl/PricePackageServiceImpl.java
package com.codecampus.service.impl;

import com.codecampus.entity.PricePackage;
import com.codecampus.repository.PricePackageRepository;
import com.codecampus.service.PricePackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PricePackageServiceImpl implements PricePackageService {

    private final PricePackageRepository pricePackageRepository;

    @Autowired
    public PricePackageServiceImpl(PricePackageRepository pricePackageRepository) {
        this.pricePackageRepository = pricePackageRepository;
    }

    @Override
    public List<PricePackage> getPackagesByCourseId(Integer courseId) {
        // Gọi phương thức findByCourseId đã định nghĩa trong Repository
        return pricePackageRepository.findByCourseId(courseId);
    }
}