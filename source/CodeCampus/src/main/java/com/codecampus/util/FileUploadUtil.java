package com.codecampus.util;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class FileUploadUtil {

    // Lưu ảnh vào folder: src/main/resources/static/uploads/sliders
    public static String saveFile(MultipartFile file) throws IOException {
        // 1. Tạo tên file ngẫu nhiên để tránh trùng
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        // 2. Định nghĩa thư mục lưu (Trong thực tế nên config trong application.properties)
        Path uploadPath = Paths.get("src/main/resources/static/uploads/sliders");

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 3. Lưu file
        try (InputStream inputStream = file.getInputStream()) {
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioe) {
            throw new IOException("Không thể lưu file: " + fileName, ioe);
        }

        // 4. Trả về đường dẫn để lưu vào DB (truy cập qua web)
        return "/uploads/sliders/" + fileName;
    }
}