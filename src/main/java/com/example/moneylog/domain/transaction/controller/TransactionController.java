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

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> create(@AuthenticationPrincipal Long userId,
                                                                   @Valid @RequestBody TransactionRequest req) {
        TransactionResponse created = transactionService.create(currentUser(userId), req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("거래내역이 등록되었습니다.", created));
    }

    @GetMapping
    public ApiResponse<Map<String, List<TransactionResponse>>> getList(
            @AuthenticationPrincipal Long userId,
            @RequestParam String yearMonth,
            @RequestParam(required = false) CategoryType type,
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 20) Pageable pageable) {
        var cond = new TransactionSearchCond(yearMonth, type, categoryId);
        Page<TransactionResponse> page = transactionService.getList(currentUser(userId), cond, pageable);
        var data = Map.of("transactions", page.getContent());
        return ApiResponse.success("거래내역 목록을 조회했습니다.", data, PageMeta.from(page));
    }

    @GetMapping("/{id}")
    public ApiResponse<TransactionResponse> get(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        return ApiResponse.success("거래내역을 조회했습니다.", transactionService.get(currentUser(userId), id));
    }

    @PutMapping("/{id}")
    public ApiResponse<TransactionResponse> update(@AuthenticationPrincipal Long userId,
                                                   @PathVariable Long id,
                                                   @Valid @RequestBody TransactionRequest req) {
        return ApiResponse.success("거래내역이 수정되었습니다.", transactionService.update(currentUser(userId), id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        transactionService.delete(currentUser(userId), id);
        return ApiResponse.success("거래내역이 삭제되었습니다.", null);
    }
}