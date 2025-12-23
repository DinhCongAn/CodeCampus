package com.codecampus.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {

        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        int statusCode = 0;
        if (status != null) {
            statusCode = Integer.parseInt(status.toString());
        }

        model.addAttribute("errorCode", statusCode);

        switch (statusCode) {

            case 400:
                model.addAttribute("errorTitle", "Yêu cầu không hợp lệ (400)");
                model.addAttribute("errorMessage", "Dữ liệu gửi lên không đúng định dạng hoặc thiếu tham số.");
                break;

            case 401:
                model.addAttribute("errorTitle", "Chưa xác thực (401)");
                model.addAttribute("errorMessage", "Bạn cần đăng nhập để truy cập tài nguyên này.");
                break;

            case 403:
                model.addAttribute("errorTitle", "Truy cập bị từ chối (403)");
                model.addAttribute("errorMessage", "Bạn không có quyền truy cập vào trang này.");
                break;

            case 404:
                model.addAttribute("errorTitle", "Không tìm thấy trang (404)");
                model.addAttribute("errorMessage", "Đường dẫn bạn truy cập không tồn tại hoặc đã bị xóa.");
                break;

            case 405:
                model.addAttribute("errorTitle", "Phương thức không được hỗ trợ (405)");
                model.addAttribute("errorMessage", "Phương thức HTTP không được phép cho URL này.");
                break;

            case 408:
                model.addAttribute("errorTitle", "Hết thời gian yêu cầu (408)");
                model.addAttribute("errorMessage", "Yêu cầu của bạn mất quá nhiều thời gian để xử lý.");
                break;

            case 409:
                model.addAttribute("errorTitle", "Xung đột dữ liệu (409)");
                model.addAttribute("errorMessage", "Dữ liệu bị xung đột, vui lòng kiểm tra lại.");
                break;

            case 413:
                model.addAttribute("errorTitle", "Dữ liệu quá lớn (413)");
                model.addAttribute("errorMessage", "Tệp tải lên vượt quá dung lượng cho phép.");
                break;

            case 415:
                model.addAttribute("errorTitle", "Định dạng không được hỗ trợ (415)");
                model.addAttribute("errorMessage", "Loại dữ liệu gửi lên không được hệ thống hỗ trợ.");
                break;

            case 429:
                model.addAttribute("errorTitle", "Quá nhiều yêu cầu (429)");
                model.addAttribute("errorMessage", "Bạn gửi yêu cầu quá nhanh. Vui lòng thử lại sau.");
                break;

            case 500:
                model.addAttribute("errorTitle", "Lỗi máy chủ (500)");
                model.addAttribute("errorMessage", "Hệ thống gặp sự cố nội bộ. Vui lòng thử lại sau.");
                break;

            case 502:
                model.addAttribute("errorTitle", "Bad Gateway (502)");
                model.addAttribute("errorMessage", "Máy chủ nhận phản hồi không hợp lệ từ dịch vụ khác.");
                break;

            case 503:
                model.addAttribute("errorTitle", "Dịch vụ không khả dụng (503)");
                model.addAttribute("errorMessage", "Hệ thống đang bảo trì hoặc quá tải.");
                break;

            case 504:
                model.addAttribute("errorTitle", "Gateway Timeout (504)");
                model.addAttribute("errorMessage", "Máy chủ phản hồi quá chậm.");
                break;

            default:
                model.addAttribute("errorTitle", "Lỗi không xác định");
                model.addAttribute("errorMessage", "Có lỗi xảy ra. Vui lòng liên hệ bộ phận hỗ trợ.");
                break;
        }

        return "error"; // error.html
    }
}
