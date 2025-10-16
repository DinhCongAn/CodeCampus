package com.codecampus.controller;

import com.codecampus.entity.Shoes;
import com.codecampus.service.ShoesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class ShoesController {

    @Autowired
    private ShoesService shoesService;

    @GetMapping("/")
    public String viewListShoes(Model model){
        model.addAttribute("shoesList", shoesService.getAll());
        model.addAttribute("shoes", new Shoes());
        model.addAttribute("type", List.of("Tennis Shoes", "Running Shoes"));
        return "shoes";
    }

    @PostMapping("/add")
    public String addShoe(@ModelAttribute Shoes shoes, Model model){
        if(shoesService.existsByShoesName(shoes.getShoesName())) {
            model.addAttribute("error", "Shoes already exists");
        } else if (shoes.getPrice() <= 0 || shoes.getPrice() >= 100) {
            model.addAttribute("error", "Price must greater than 0 and less than < 100");
        }else  {
            shoesService.addShoes(shoes);
            model.addAttribute("message", "Create new shoes successfully");
        }
        model.addAttribute("shoesList", shoesService.getAll());
        model.addAttribute("shoes", new Shoes());
        model.addAttribute("type", List.of("Tennis Shoes", "Running Shoes"));
        return "shoes";
    }
    @GetMapping("/delete/{id}")
    public String deleteShoes(@PathVariable("id") int id, Model model) {
        shoesService.deleteShoesById(id);
        model.addAttribute("message", "Deleted successfully");
        model.addAttribute("shoesList", shoesService.getAll());
        model.addAttribute("shoes", new Shoes());
        model.addAttribute("type", List.of("Tennis Shoes", "Running Shoes"));
        return "shoes";
    }
}
//git
