package com.codecampus.service;

import com.codecampus.entity.User;
import com.codecampus.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Set;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user: " + email));

        boolean enabled = "active".equals(user.getStatus());
        boolean accountNonLocked = !"blocked".equals(user.getStatus());

        // Tự động thêm tiền tố ROLE_ nếu trong DB chưa có
        String roleName = user.getRole().getName();
        if (!roleName.startsWith("ROLE_")) {
            roleName = "ROLE_" + roleName;
        }

        Set<GrantedAuthority> authorities = Collections.singleton(
                new SimpleGrantedAuthority(roleName)
        );

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                enabled, true, true, accountNonLocked,
                authorities
        );
    }
}