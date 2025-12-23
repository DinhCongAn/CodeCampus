package com.codecampus.config;

import com.codecampus.service.CustomOAuth2UserService;
import com.codecampus.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService, CustomOAuth2UserService customOAuth2UserService) {
        this.customUserDetailsService = customUserDetailsService;
        this.customOAuth2UserService = customOAuth2UserService;
    }

    // Handler xử lý lỗi tập trung
    @Bean
    public AuthenticationFailureHandler customFailureHandler() {
        return (request, response, exception) -> {
            String errorParam = "invalid";

            // Trường hợp 1: Lỗi từ Login bằng Google
            if (exception instanceof OAuth2AuthenticationException) {
                OAuth2AuthenticationException oauthException = (OAuth2AuthenticationException) exception;
                String errorCode = oauthException.getError().getErrorCode(); // Lấy "blocked" hoặc "pending" ở đây

                if ("blocked".equals(errorCode)) {
                    errorParam = "blocked";
                } else if ("pending".equals(errorCode)) {
                    errorParam = "pending";
                }
            }
            // Trường hợp 2: Lỗi từ Login bằng Email/Mật khẩu (Form Login)
            else if (exception instanceof DisabledException) {
                errorParam = "pending";
            }
            else if (exception instanceof LockedException) {
                errorParam = "blocked";
            }
            else if (exception instanceof BadCredentialsException) {
                errorParam = "invalid";
            }

            // Log ra console để bạn kiểm tra khi test
            System.out.println("Login failed with param: " + errorParam);

            response.sendRedirect("/login?error=" + errorParam);
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/payment/**", "/api/ai/**"))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(
                                "/home","/blog/**","/courses/**", "/login", "/register", "/logout",
                                "/forgot-password", "/reset-password", "/verify",
                                "/login/**", "/css/**", "/js/**", "/images/**",
                                "/register-process","/payment/**", "/api/ai/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .defaultSuccessUrl("/home", true)
                        .failureHandler(customFailureHandler()) // Dùng handler chung
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(ui -> ui.userService(customOAuth2UserService))
                        .failureHandler(customFailureHandler()) // Đồng bộ cho Google
                        .defaultSuccessUrl("/home", true)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                )
                .userDetailsService(customUserDetailsService);

        return http.build();
    }
}