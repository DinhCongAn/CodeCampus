package com.codecampus.controller; // Lưu ý package

import com.codecampus.service.RegistrationService;
import com.codecampus.payos.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import vn.payos.PayOS;
import vn.payos.model.webhooks.WebhookData;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired private PayOS payOS;
    @Autowired private RegistrationService registrationService; // Inject Service vào

    @PostMapping(path = "/payos_transfer_handler")
    public ApiResponse<WebhookData> payosTransferHandler(@RequestBody Object body) {
        try {
            // 1. Xác thực Webhook (Quan trọng)
            WebhookData data = payOS.webhooks().verify(body);

            // 2. Lấy OrderCode từ Webhook
            long orderCode = data.getOrderCode();
            System.out.println("Webhook received for Order: " + orderCode);

            // 3. GỌI SERVICE ĐỂ UPDATE DB THÀNH "COMPLETED"
            registrationService.activateRegistration(orderCode);

            return ApiResponse.success("Webhook delivered", data);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(e.getMessage());
        }
    }
}