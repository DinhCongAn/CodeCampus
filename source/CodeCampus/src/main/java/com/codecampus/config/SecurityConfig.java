package com.codecampus.config;

import com.codecampus.service.CustomOAuth2UserService;
import com.codecampus.service.CustomUserDetailsService;
import jakarta.servlet.RequestDispatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService, CustomOAuth2UserService customOAuth2UserService) {
        this.customUserDetailsService = customUserDetailsService;
        this.customOAuth2UserService = customOAuth2UserService;
    }

    // 1. Xử lý khi đăng nhập thành công (Admin -> Dashboard, User -> Home)
    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return (request, response, authentication) -> {
            var authorities = authentication.getAuthorities();
            boolean isAdmin = authorities.stream()
                    .anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"));

            if (isAdmin) {
                response.sendRedirect("/home");
            } else {
                response.sendRedirect("/home");
            }
        };
    }

    // 2. Xử lý khi đăng nhập thất bại (Sai pass, bị block...)
    @Bean
    public AuthenticationFailureHandler customFailureHandler() {
        return (request, response, exception) -> {
            String errorParam = "invalid";
            if (exception instanceof OAuth2AuthenticationException) {
                OAuth2AuthenticationException oauthException = (OAuth2AuthenticationException) exception;
                String errorCode = oauthException.getError().getErrorCode();
                if ("blocked".equals(errorCode)) errorParam = "blocked";
                else if ("pending".equals(errorCode)) errorParam = "pending";
            } else if (exception instanceof DisabledException) {
                errorParam = "pending";
            } else if (exception instanceof LockedException) {
                errorParam = "blocked";
            } else if (exception instanceof BadCredentialsException) {
                errorParam = "invalid";
            }
            response.sendRedirect("/login?error=" + errorParam);
        };
    }

    // 3. Xử lý khi User cố vào trang Admin (Lỗi 403 Forbidden)
    @Bean
    public AccessDeniedHandler customAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            // Forward request đến Controller xử lý lỗi để hiện trang đẹp
            request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 403);
            response.setStatus(403);
            request.getRequestDispatcher("/error").forward(request, response);
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/payment/**", "/api/ai/**"))
                .authorizeHttpRequests(authz -> authz
                        // Cho phép truy cập public + trang lỗi
                        .requestMatchers(
                                "/home", "/blog/**", "/courses/**", "/login", "/register", "/logout",
                                "/forgot-password", "/reset-password", "/verify",
                                "/login/**", "/css/**", "/js/**", "/images/**",
                                "/register-process", "/payment/**", "/api/ai/**",
                                "/error" // Quan trọng: Phải mở trang lỗi
                        ).permitAll()

                        // Chỉ Admin mới được vào /admin/**
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Các trang còn lại cần đăng nhập
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .successHandler(customSuccessHandler()) // Custom redirect
                        .failureHandler(customFailureHandler()) // Custom error param
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(ui -> ui.userService(customOAuth2UserService))
                        .successHandler(customSuccessHandler())
                        .failureHandler(customFailureHandler())
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                )
                // Kích hoạt xử lý lỗi 403 tùy chỉnh
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(customAccessDeniedHandler())
                );

        return http.build();
    }
}