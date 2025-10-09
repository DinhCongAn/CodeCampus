package com.codecampus.repository;


import com.codecampus.entity.Shoes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShoesRepository extends JpaRepository<Shoes, Integer> {
    boolean existsByShoesName(String shoesName);
}
