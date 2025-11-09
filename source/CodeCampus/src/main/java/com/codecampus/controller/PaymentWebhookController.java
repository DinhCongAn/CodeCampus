// src/main/java/com/codecampus/controller/PaymentWebhookController.java
package com.codecampus.controller;

import com.codecampus.dto.WebhookPayload;
import com.codecampus.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment/webhook")
public class PaymentWebhookController {

    @Autowired
    private RegistrationService registrationService;

    /**
     * API này bạn cung cấp cho bên trung gian thanh toán (Casso, PayOS)
     * Dùng ngrok để test ở local
     */
    @PostMapping
    public ResponseEntity<String> handlePaymentWebhook(@RequestBody WebhookPayload payload) {
        try {
            // (Thực tế cần: Xác thực token/chữ ký từ webhook)

            String orderCode = payload.getOrderCode(); // Lấy mã đơn hàng
            if (orderCode == null || orderCode.isEmpty()) {
                throw new IllegalArgumentException("Order code is missing from payload");
            }

            registrationService.completeRegistration(orderCode); // Hoàn tất đơn

            return ResponseEntity.ok("Webhook processed");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}