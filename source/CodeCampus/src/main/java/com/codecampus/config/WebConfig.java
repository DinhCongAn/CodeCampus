package com.codecampus.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                // Các đường dẫn CẦN bảo vệ
                .addPathPatterns(
                        "/dashboard/**",
                        "/my-courses/**",
                        "/profile/**"
                        // Thêm các URL khác cần bảo vệ
                )
                // Các đường dẫn CÔNG KHAI
                .excludePathPatterns(
                        "/login",
                        "/register",
                        "/logout",
                        "/verify", // Rất quan trọng, phải công khai
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/"
                );
    }
}