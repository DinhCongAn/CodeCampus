package com.codecampus.service;

import com.codecampus.entity.User;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

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
        OAuth2User oauthUser = super.loadUser(userRequest);
        Map<String, Object> attributes = oauthUser.getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String avatarUrl = (String) attributes.get("picture");

        User user = userService.processOAuthPostLogin(email, name, avatarUrl);

        if ("blocked".equals(user.getStatus())) {
            throw new OAuth2AuthenticationException(new OAuth2Error("blocked"), "blocked");
        }
        if ("pending".equals(user.getStatus())) {
            throw new OAuth2AuthenticationException(new OAuth2Error("pending"), "pending");
        }

        // Xử lý Role
        String roleName = user.getRole().getName();
        if (!roleName.startsWith("ROLE_")) {
            roleName = "ROLE_" + roleName;
        }

        return new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority(roleName)),
                attributes,
                "email"
        );
    }
}