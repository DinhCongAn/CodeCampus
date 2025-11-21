package com.codecampus.service;

import com.codecampus.entity.User;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;

    public CustomOAuth2UserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. Lấy thông tin user từ Google
        OAuth2User oauthUser = super.loadUser(userRequest);
        Map<String, Object> attributes = oauthUser.getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String avatarUrl = (String) attributes.get("picture"); // Lấy avatar nếu muốn
        System.out.println("OAuth2 attributes: " + attributes);

        // 2. Xử lý (lưu/cập nhật vào DB) bằng UserService của chúng ta
        User user = userService.processOAuthPostLogin(email, name, avatarUrl);

        // 3. Trả về một đối tượng OAuth2User mới cho Spring Security
        return new DefaultOAuth2User(
                Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority(user.getRole().getName())),
                attributes,
                "email" // Key dùng làm principal name (tên định danh chính)
        );
    }
}