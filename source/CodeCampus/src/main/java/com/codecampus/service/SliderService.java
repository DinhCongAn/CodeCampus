package com.codecampus.service;

import com.codecampus.entity.Slider;
import com.codecampus.repository.SliderRepository;
import com.codecampus.util.FileUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class SliderService {

    @Autowired private SliderRepository sliderRepository;

    // 1. Lấy danh sách
    public Page<Slider> getSlidersAdmin(String keyword, String status, int page, int size) {
        return sliderRepository.findSlidersAdmin(keyword, status, PageRequest.of(page, size));
    }

    // 2. Lấy chi tiết
    public Slider getSliderById(Integer id) {
        return sliderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Slider ID: " + id));
    }

    // 3. Lưu
    public void saveSlider(Slider slider, MultipartFile imageFile) throws IOException {

        // [MỚI] Bước 1: Check trùng tên ngay lập tức
        // Cắt khoảng trắng thừa trước khi check
        String cleanTitle = slider.getTitle().trim();
        slider.setTitle(cleanTitle);
        checkDuplicateTitle(slider.getId(), cleanTitle);

        // Bước 2: Xử lý File Upload (Code cũ)
        if (imageFile != null && !imageFile.isEmpty()) {
            String path = FileUploadUtil.saveFile(imageFile);
            slider.setImageUrl(path);
        }

        // Bước 3: Set Default Status (Code cũ)
        if (slider.getId() == null) {
            if (slider.getStatus() == null) slider.setStatus("active");
        }

        sliderRepository.save(slider);
    }

    /**
     * [MỚI] Hàm logic kiểm tra trùng tiêu đề
     */
    private void checkDuplicateTitle(Integer id, String title) {
        Slider existing = sliderRepository.findByTitleIgnoreCase(title);

        if (existing != null) {
            // Trường hợp Thêm mới (id null) mà tìm thấy tên -> Trùng
            if (id == null) {
                throw new RuntimeException("Tiêu đề '" + title + "' đã tồn tại.");
            }

            // Trường hợp Sửa (id có giá trị)
            // Tìm thấy tên, nhưng ID khác ID đang sửa -> Trùng với slider khác
            if (!existing.getId().equals(id)) {
                throw new RuntimeException("Tiêu đề '" + title + "' đã được sử dụng bởi slider khác.");
            }
        }
    }

    // 4. Toggle Status (Hide/Show)
    public void toggleStatus(Integer id) {
        Slider slider = getSliderById(id);
        if ("active".equalsIgnoreCase(slider.getStatus())) {
            slider.setStatus("inactive"); // Hide
        } else {
            slider.setStatus("active"); // Show
        }
        sliderRepository.save(slider);
    }
}