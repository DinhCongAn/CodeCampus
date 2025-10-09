package com.codecampus.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "shoes")
public class Shoes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shoes_id", nullable = false)
    private Integer id;

    @Nationalized
    @Column(name = "shoes_Name", nullable = false, length = 50)
    private String shoesName;

    @Column(name = "price", nullable = false)
    private Double price;

    @Nationalized
    @Column(name = "manufacturer", nullable = false, length = 100)
    private String manufacturer;

    @Nationalized
    @Column(name = "type", nullable = false, length = 30)
    private String type;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getShoesName() {
        return shoesName;
    }

    public void setShoesName(String shoesName) {
        this.shoesName = shoesName;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}