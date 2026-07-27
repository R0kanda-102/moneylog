package com.example.moneylog.domain.statistics.controller;

import com.example.moneylog.domain.statistics.dto.StatisticsResponse;
import com.example.moneylog.domain.statistics.service.StatisticsService;
import com.example.moneylog.domain.user.entity.User;
import com.example.moneylog.domain.user.repository.UserRepository;
import com.example.moneylog.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final UserRepository userRepository;

    private static final Long TEMP_USER_ID = 1L;

    private User currentUser() {
        return userRepository.findById(TEMP_USER_ID)
                .orElseThrow(() -> new IllegalStateException("임시 사용자가 없습니다."));
    }

    @GetMapping("/monthly")
    public ApiResponse<StatisticsResponse> monthly(@RequestParam String yearMonth) {
        return ApiResponse.success("월별 통계를 조회했습니다.", statisticsService.monthly(currentUser(), yearMonth));
    }
}