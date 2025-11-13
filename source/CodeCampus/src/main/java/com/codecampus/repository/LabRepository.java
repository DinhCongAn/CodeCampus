package com.codecampus.repository;

import com.codecampus.entity.Lab;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabRepository extends JpaRepository<Lab, Integer> {
}
