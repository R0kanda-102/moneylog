package com.example.moneylog.domain.statistics.controller;

import com.example.moneylog.domain.statistics.dto.StatisticsResponse;
import com.example.moneylog.domain.statistics.service.StatisticsService;
import com.example.moneylog.domain.user.entity.User;
import com.example.moneylog.domain.user.repository.UserRepository;
import com.example.moneylog.global.common.ApiResponse;
import com.example.moneylog.global.exception.CustomException;
import com.example.moneylog.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Statistics", description = "월별 통계 API")
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

    @Operation(summary = "월별 통계 조회", description = "지정한 월의 총수입, 총지출, 잔액, 카테고리별 지출 합계를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요 (UNAUTHORIZED)")
    })
    @GetMapping("/monthly")
    public ApiResponse<StatisticsResponse> monthly(@AuthenticationPrincipal Long userId,
                                                   @Parameter(description = "조회할 연월 (예: 2026-07)", example = "2026-07") @RequestParam String yearMonth) {
        return ApiResponse.success("월별 통계를 조회했습니다.", statisticsService.monthly(currentUser(userId), yearMonth));
    }
}