package com.codecampus.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        HttpSession session = request.getSession(false);

        // Kiểm tra session có tồn tại và có chứa user đã đăng nhập không
        if (session == null || session.getAttribute("loggedInUserEmail") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false; // Ngăn request tiếp tục
        }

        return true; // Cho phép request tiếp tục
    }
}