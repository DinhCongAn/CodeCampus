package com.codecampus.repository;

import com.codecampus.entity.Slider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SliderRepository extends JpaRepository<Slider, Integer> {
    // Tìm các slider theo trạng thái
    List<Slider> findByStatus(String status);
}
