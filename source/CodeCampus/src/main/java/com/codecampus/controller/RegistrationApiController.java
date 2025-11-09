// src/main/java/com/codecampus/controller/RegistrationApiController.java
package com.codecampus.controller;

import com.codecampus.entity.Registration;
import com.codecampus.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/registration")
public class RegistrationApiController {

    @Autowired
    private RegistrationService registrationService;

    /**
     * API để JS ở trang confirm-payment.html gọi kiểm tra
     */
    @GetMapping("/status/{id}")
    public Map<String, String> getRegistrationStatus(@PathVariable("id") Integer id) {
        try {
            Registration reg = registrationService.getRegistrationById(id);
            // Trả về: {"status": "PENDING"} hoặc {"status": "COMPLETED"}
            return Map.of("status", reg.getStatus());
        } catch (Exception e) {
            return Map.of("status", "ERROR");
        }
    }
}