package com.codecampus.config;

import com.codecampus.service.CustomOAuth2UserService;
import com.codecampus.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService, CustomOAuth2UserService customOAuth2UserService) {
        this.customUserDetailsService = customUserDetailsService;
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        // Cho phép các trang public
                        .requestMatchers(
                                "/home","/blog/**","courses/**", "/login", "/register", "/logout",
                                "/forgot-password", "/reset-password", "/verify",
                                "/login/**", // Cho phép CSS/JS trong thư mục /login
                                "/css/**", "/js/**", "/images/**",
                                // ===== THAY ĐỔI =====
                                "/register-process", // Chỉ cần mở cái này
                                "/payment-info"
                        ).permitAll()
                        // Tất cả các request khác phải được xác thực
                        .anyRequest().authenticated()
                )
                // Cấu hình Form Login (cho email/password)
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email") // Khớp với name="email" trong form
                        .defaultSuccessUrl("/home", true)
                        .failureUrl("/login?error=true") // Báo lỗi qua param.error
                        .permitAll()
                )
                // Cấu hình Google Login
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService) // Dùng service tùy chỉnh
                        )
                        .defaultSuccessUrl("/home", true)
                )
                // Cấu hình Logout
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                // Cung cấp dịch vụ tìm user (cho form email/pass)
                .userDetailsService(customUserDetailsService);

        return http.build();
    }
}