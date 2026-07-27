package com.example.moneylog.global.config;

import com.example.moneylog.domain.category.service.CategoryService;
import com.example.moneylog.domain.user.entity.User;
import com.example.moneylog.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Profile("local")
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryService categoryService;

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail("test@moneylog.com")) {
            return;
        }

        User testUser = userRepository.save(User.builder()
                .email("test@moneylog.com")
                .password("(3일차에서 BCrypt로 교체 예정)")
                .nickname("테스트유저")
                .build());

        categoryService.seedDefaultCategories(testUser);

        log.info("테스트 유저 생성 완료: id={}, email={}", testUser.getId(), testUser.getEmail());
    }
}