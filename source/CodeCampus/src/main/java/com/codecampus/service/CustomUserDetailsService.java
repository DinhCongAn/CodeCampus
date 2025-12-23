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
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user với email: " + email));

        // Xác định trạng thái dựa trên database
        boolean enabled = "active".equals(user.getStatus()); // Nếu là active thì true, ngược lại (pending) là false
        boolean accountNonLocked = !"blocked".equals(user.getStatus()); // Nếu blocked thì false

        Set<GrantedAuthority> authorities = Collections.singleton(
                new SimpleGrantedAuthority(user.getRole().getName())
        );

        // Sử dụng Constructor đầy đủ của User (Spring Security)
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                enabled,             // enabled (Nếu false sẽ ném DisabledException)
                true,                // accountNonExpired
                true,                // credentialsNonExpired
                accountNonLocked,    // accountNonLocked (Nếu false sẽ ném LockedException)
                authorities
        );
    }
}