package com.example.moneylog.domain.statistics.controller;

import com.example.moneylog.domain.statistics.dto.StatisticsResponse;
import com.example.moneylog.domain.statistics.service.StatisticsService;
import com.example.moneylog.domain.user.entity.User;
import com.example.moneylog.domain.user.repository.UserRepository;
import com.example.moneylog.global.common.ApiResponse;
import com.example.moneylog.global.exception.CustomException;
import com.example.moneylog.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    private User currentUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));
    }

    @GetMapping("/monthly")
    public ApiResponse<StatisticsResponse> monthly(@AuthenticationPrincipal Long userId,
                                                   @RequestParam String yearMonth) {
        return ApiResponse.success("월별 통계를 조회했습니다.", statisticsService.monthly(currentUser(userId), yearMonth));
    }
}