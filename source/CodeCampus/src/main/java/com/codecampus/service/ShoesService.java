package com.codecampus.service;

import com.codecampus.entity.Shoes;

import java.util.List;

public interface ShoesService {
    List<Shoes> getAll();
    Shoes addShoes(Shoes shoes);
    void deleteShoesById(Integer id);
    boolean existsByShoesName(String shoesName);
}
