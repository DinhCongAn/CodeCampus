package com.codecampus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync // Kích hoạt tính năng bất đồng bộ
public class CodeCampusApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeCampusApplication.class, args);
    }

}