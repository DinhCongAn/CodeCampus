package com.codecampus.service.impl;

import com.codecampus.entity.Shoes;
import com.codecampus.repository.ShoesRepository;
import com.codecampus.service.ShoesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShoesServiceImpl implements ShoesService {

    @Autowired
    private ShoesRepository shoesRepository;

    public List<Shoes> getAll(){
        return shoesRepository.findAll(Sort.by("shoesName"));
    }

    public Shoes addShoes(Shoes shoes){
        return shoesRepository.save(shoes);
    }

    public void deleteShoesById(Integer id){
        shoesRepository.deleteById(id);
    }

    public boolean existsByShoesName(String shoesName){
        return shoesRepository.existsByShoesName(shoesName);
    }
}

