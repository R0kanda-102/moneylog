package com.example.moneylog.domain.category.controller;

import com.example.moneylog.domain.category.dto.CategoryRequest;
import com.example.moneylog.domain.category.dto.CategoryResponse;
import com.example.moneylog.domain.category.service.CategoryService;
import com.example.moneylog.domain.user.entity.User;
import com.example.moneylog.domain.user.repository.UserRepository;
import com.example.moneylog.global.common.ApiResponse;
import com.example.moneylog.global.exception.CustomException;
import com.example.moneylog.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Category", description = "카테고리 관리 API (본인 카테고리만 접근 가능)")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final UserRepository userRepository;

    private User currentUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));
    }

    @Operation(summary = "카테고리 목록 조회", description = "내 카테고리 전체 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요 (UNAUTHORIZED)")
    })
    @GetMapping
    public ApiResponse<List<CategoryResponse>> list(@AuthenticationPrincipal Long userId) {
        return ApiResponse.success("카테고리 목록을 조회했습니다.", categoryService.getMyCategories(currentUser(userId)));
    }

    @Operation(summary = "카테고리 등록", description = "새 카테고리를 추가합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패 (VALIDATION_ERROR)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요 (UNAUTHORIZED)")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> create(@AuthenticationPrincipal Long userId,
                                                                @Valid @RequestBody CategoryRequest req) {
        CategoryResponse created = categoryService.create(currentUser(userId), req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("카테고리가 등록되었습니다.", created));
    }

    @Operation(summary = "카테고리 수정", description = "본인 소유의 카테고리를 수정합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패 (VALIDATION_ERROR)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요 (UNAUTHORIZED)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음 (CATEGORY_NOT_FOUND)")
    })
    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> update(@AuthenticationPrincipal Long userId,
                                                @PathVariable Long id,
                                                @Valid @RequestBody CategoryRequest req) {
        return ApiResponse.success("카테고리가 수정되었습니다.", categoryService.update(currentUser(userId), id, req));
    }

    @Operation(summary = "카테고리 삭제", description = "본인 소유의 카테고리를 삭제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요 (UNAUTHORIZED)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음 (CATEGORY_NOT_FOUND)")
    })
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        categoryService.delete(currentUser(userId), id);
        return ApiResponse.success("카테고리가 삭제되었습니다.", null);
    }
}