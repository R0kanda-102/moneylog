package com.example.moneylog.domain.transaction.controller;

import com.example.moneylog.domain.category.entity.CategoryType;
import com.example.moneylog.domain.transaction.dto.TransactionRequest;
import com.example.moneylog.domain.transaction.dto.TransactionResponse;
import com.example.moneylog.domain.transaction.dto.TransactionSearchCond;
import com.example.moneylog.domain.transaction.service.TransactionService;
import com.example.moneylog.domain.user.entity.User;
import com.example.moneylog.domain.user.repository.UserRepository;
import com.example.moneylog.global.common.ApiResponse;
import com.example.moneylog.global.common.PageMeta;
import com.example.moneylog.global.exception.CustomException;
import com.example.moneylog.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Transaction", description = "거래내역 CRUD API (본인 거래만 접근 가능)")
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;

    private User currentUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));
    }

    @Operation(summary = "거래내역 등록", description = "수입/지출 거래를 등록합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패 (VALIDATION_ERROR)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요 (UNAUTHORIZED)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음 (CATEGORY_NOT_FOUND)")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> create(@AuthenticationPrincipal Long userId,
                                                                   @Valid @RequestBody TransactionRequest req) {
        TransactionResponse created = transactionService.create(currentUser(userId), req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("거래내역이 등록되었습니다.", created));
    }

    @Operation(summary = "거래내역 목록 조회", description = "월(yearMonth) 기준으로 타입/카테고리 필터와 페이징을 적용해 거래내역을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요 (UNAUTHORIZED)")
    })
    @GetMapping
    public ApiResponse<Map<String, List<TransactionResponse>>> getList(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "조회할 연월 (예: 2026-07)", example = "2026-07") @RequestParam String yearMonth,
            @Parameter(description = "거래 타입 필터") @RequestParam(required = false) CategoryType type,
            @Parameter(description = "카테고리 ID 필터") @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 20) Pageable pageable) {
        var cond = new TransactionSearchCond(yearMonth, type, categoryId);
        Page<TransactionResponse> page = transactionService.getList(currentUser(userId), cond, pageable);
        var data = Map.of("transactions", page.getContent());
        return ApiResponse.success("거래내역 목록을 조회했습니다.", data, PageMeta.from(page));
    }

    @Operation(summary = "거래내역 상세 조회", description = "본인 소유의 거래내역 1건을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요 (UNAUTHORIZED)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "거래내역을 찾을 수 없음 (TRANSACTION_NOT_FOUND)")
    })
    @GetMapping("/{id}")
    public ApiResponse<TransactionResponse> get(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        return ApiResponse.success("거래내역을 조회했습니다.", transactionService.get(currentUser(userId), id));
    }

    @Operation(summary = "거래내역 수정", description = "본인 소유의 거래내역을 수정합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패 (VALIDATION_ERROR)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요 (UNAUTHORIZED)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "거래내역 또는 카테고리를 찾을 수 없음")
    })
    @PutMapping("/{id}")
    public ApiResponse<TransactionResponse> update(@AuthenticationPrincipal Long userId,
                                                   @PathVariable Long id,
                                                   @Valid @RequestBody TransactionRequest req) {
        return ApiResponse.success("거래내역이 수정되었습니다.", transactionService.update(currentUser(userId), id, req));
    }

    @Operation(summary = "거래내역 삭제", description = "본인 소유의 거래내역을 삭제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요 (UNAUTHORIZED)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "거래내역을 찾을 수 없음 (TRANSACTION_NOT_FOUND)")
    })
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        transactionService.delete(currentUser(userId), id);
        return ApiResponse.success("거래내역이 삭제되었습니다.", null);
    }
}