package com.codecampus.controller.admin;

import com.codecampus.entity.Registration;
import com.codecampus.repository.RegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminOrderController {

    @Autowired
    private RegistrationRepository registrationRepository;

    @GetMapping("/orders")
    public String showOrderList(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "7") int size,
            Model model) {

        // 1. Gọi Repository để lấy dữ liệu phân trang
        // Nếu status là rỗng thì truyền null để query lấy tất cả
        String searchStatus = (status != null && !status.isEmpty()) ? status : null;

        Page<Registration> orderPage = registrationRepository.findOrders(
                keyword,
                searchStatus,
                PageRequest.of(page, size)
        );

        // 2. Đẩy dữ liệu ra View
        model.addAttribute("orderPage", orderPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);

        // Biến để active menu bên Sidebar
        model.addAttribute("activePage", "orders");

        return "admin/orders"; // Trả về file html mới
    }

    /**
     * Chức năng: Admin hủy đơn hàng PENDING
     */
    @PostMapping("/orders/cancel/{id}")
    public String cancelOrder(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            Registration reg = registrationRepository.findById(id).orElse(null);

            // Chỉ cho phép hủy nếu đơn đang là PENDING
            if (reg != null && "PENDING".equals(reg.getStatus())) {

                // 1. Đổi trạng thái sang CANCELLED
                reg.setStatus("CANCELLED");

                // 2. Cập nhật thời gian sửa đổi (Để biết hủy lúc nào)
                reg.setUpdatedAt(LocalDateTime.now());

                // 3. Lưu ngược lại vào DB (Hàm save trong JPA tự hiểu là Update nếu có ID)
                registrationRepository.save(reg);

                // Thay đổi thông báo cho hợp lý
                redirectAttributes.addFlashAttribute("successMessage", "Đã chuyển trạng thái đơn hàng #" + reg.getOrderCode() + " sang ĐÃ HỦY.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể hủy đơn hàng này (Không tồn tại hoặc đã hoàn thành).");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xử lý: " + e.getMessage());
        }

        return "redirect:/admin/orders";
    }

    /**
     * API lấy chi tiết đơn hàng (Trả về JSON cho Modal)
     */
    @GetMapping("/orders/api/{id}")
    @ResponseBody // Báo hiệu trả về dữ liệu thô, không phải tên file HTML
    public ResponseEntity<?> getOrderDetailsApi(@PathVariable("id") Integer id) {
        Registration reg = registrationRepository.findById(id).orElse(null);

        if (reg == null) {
            return ResponseEntity.notFound().build();
        }

        // Đóng gói dữ liệu thủ công để tránh lỗi JSON vòng lặp
        Map<String, Object> data = new HashMap<>();
        data.put("id", reg.getId());
        data.put("orderCode", reg.getOrderCode());
        data.put("status", reg.getStatus());
        data.put("totalCost", reg.getTotalCost());

        // Format ngày tháng cho đẹp
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        data.put("registrationTime", reg.getRegistrationTime().format(formatter));
        data.put("validFrom", reg.getValidFrom() != null ? reg.getValidFrom().format(formatter) : "Chưa kích hoạt");
        data.put("validTo", reg.getValidTo() != null ? reg.getValidTo().format(formatter) : "Vô thời hạn");

        // Thông tin User
        Map<String, String> userMap = new HashMap<>();
        userMap.put("fullName", reg.getUser().getFullName());
        userMap.put("email", reg.getUser().getEmail());
        userMap.put("mobile", reg.getUser().getMobile());
        // Xử lý avatar null
        userMap.put("avatar", reg.getUser().getAvatarUrl() != null ? reg.getUser().getAvatarUrl() : "https://ui-avatars.com/api/?name=" + reg.getUser().getFullName());
        data.put("user", userMap);

        // Thông tin Course
        Map<String, String> courseMap = new HashMap<>();
        courseMap.put("name", reg.getCourse().getName()); // Hoặc .getTitle() tùy entity
        courseMap.put("thumbnail", reg.getCourse().getThumbnailUrl()); // Nhớ dùng đúng tên biến thumbnail
        data.put("course", courseMap);

        // Thông tin Gói
        data.put("packageName", reg.getPricePackage().getName());
        data.put("duration", reg.getPricePackage().getDurationMonths() + " tháng");

        return ResponseEntity.ok(data);
    }
}