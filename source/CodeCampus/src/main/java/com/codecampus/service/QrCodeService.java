// src/main/java/com/codecampus/service/QrCodeService.java
package com.codecampus.service;

import com.codecampus.entity.Registration;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
public class QrCodeService {

    // Lấy thông tin từ application.properties
    @Value("${payment.vietqr.bank-bin}")
    private String bankBin;
    @Value("${payment.vietqr.account-no}")
    private String bankAccountNo;
    @Value("${payment.vietqr.account-name}")
    private String bankAccountName;
    @Value("${payment.vietqr.template}")
    private String template;

    /**
     * Tạo chuỗi ảnh Base64 của mã VietQR
     */
    public String generateVietQrBase64(Registration registration) throws Exception {
        // 1. Lấy thông tin thanh toán
        // Chuyển BigDecimal thành số nguyên (số tiền)
        String amount = registration.getTotalCost().toBigInteger().toString();
        // Nội dung CK (Mã đơn hàng) là thông tin quan trọng nhất
        String memo = registration.getOrderCode();

        // 2. Tạo Payload theo chuẩn VietQR
        String payload = String.format("https://api.vietqr.io/image/%s-%s-%s.png?amount=%s&addInfo=%s&accountName=%s",
                bankBin,
                bankAccountNo,
                template,
                amount,
                memo,
                bankAccountName
        );

        // 3. Tạo mã QR từ payload
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(payload, BarcodeFormat.QR_CODE, 300, 300); // Size 300x300
        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

        // 4. Chuyển ảnh thành chuỗi Base64
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);

        // Trả về chuỗi Base64 để nhúng vào <img>
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }
}