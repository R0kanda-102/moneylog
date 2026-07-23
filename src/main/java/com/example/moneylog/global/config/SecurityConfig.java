package com.example.moneylog.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // 임시 설정: 2일차 동안은 모든 요청을 허용해 H2 콘솔/API 테스트를 막지 않는다.
    // 3일차(1-8)에서 JWT 기반 인증/인가로 교체 예정.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .headers(headers -> headers
                        .frameOptions(frame -> frame.disable()) // H2 콘솔은 iframe을 쓰므로 필요
                );

        return http.build();
    }
}