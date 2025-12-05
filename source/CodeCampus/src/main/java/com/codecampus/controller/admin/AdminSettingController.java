package com.codecampus.controller.admin;

import com.codecampus.entity.Setting;
import com.codecampus.service.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/admin/settings")
public class AdminSettingController {

    @Autowired
    private SettingService settingService;

    @GetMapping
    public String listSettings(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page, // Sửa: Mặc định là trang 1 (View đếm từ 1)
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir, // Sửa: Mặc định mới nhất lên đầu
            Model model) {

        // 1. Lấy config số dòng trên trang (page_size) từ DB
        String pageSizeStr = settingService.getSettingValue("size_setting"); // Hoặc page_size_admin
        int pageSize = (pageSizeStr != null && !pageSizeStr.isEmpty()) ? Integer.parseInt(pageSizeStr) : 10;

        // 2. Xử lý Sort
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        // 3. Gọi Service (Lưu ý: PageRequest của Spring tính từ 0, nên phải lấy page - 1)
        Pageable pageable = PageRequest.of(page - 1, pageSize, sort);
        Page<Setting> pageResult = settingService.getAllSettings(keyword, type, status, pageable);

        // --- KHẮC PHỤC LỖI TẠI ĐÂY ---

        // 4.1. Đổi tên attribute thành "listSettings" cho khớp với HTML
        model.addAttribute("listSettings", pageResult.getContent());

        // 4.2. Các biến phục vụ hiển thị
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedStatus", status);

        // 4.3. Gửi danh sách Type để hiển thị Dropdown lọc
        model.addAttribute("typeList", settingService.getAllTypes());

        // 5. Logic Phân trang (Pagination)
        model.addAttribute("currentPage", page); // Trang hiện tại (số 1, 2, 3...)
        model.addAttribute("totalPages", pageResult.getTotalPages());
        model.addAttribute("totalItems", pageResult.getTotalElements()); // Tổng số bản ghi

        // Tạo danh sách số trang [1, 2, 3...] để View vẽ các nút bấm
        if (pageResult.getTotalPages() > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, pageResult.getTotalPages())
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        // 6. Object rỗng cho Form Modal (Nếu dùng th:object)
        model.addAttribute("newSetting", new Setting());

        return "admin/setting";
    }

    @PostMapping("/save")
    public String saveSetting(@ModelAttribute Setting setting, RedirectAttributes redirectAttributes) {
        try {
            settingService.saveSetting(setting);
            redirectAttributes.addFlashAttribute("message", "Lưu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/settings";
    }

    @GetMapping("/delete/{id}")
    public String deleteSetting(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            settingService.deleteSetting(id);
            redirectAttributes.addFlashAttribute("message", "Đã chuyển vào thùng rác!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/settings";
    }

    @GetMapping("/toggle/{id}")
    public String toggleStatus(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            settingService.toggleStatus(id);
             redirectAttributes.addFlashAttribute("message", "Đã cập nhật trạng thái!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/settings";
    }

}