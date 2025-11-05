package com.codecampus.repository;

import com.codecampus.entity.BlogCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlogCategoryRepository extends JpaRepository<BlogCategory, Integer> {
    List<BlogCategory> findByIsActive(boolean isActive);
}