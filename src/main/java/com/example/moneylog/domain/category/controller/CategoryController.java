package com.example.moneylog.domain.category.controller;

import com.example.moneylog.domain.category.dto.CategoryRequest;
import com.example.moneylog.domain.category.dto.CategoryResponse;
import com.example.moneylog.domain.category.service.CategoryService;
import com.example.moneylog.domain.user.entity.User;
import com.example.moneylog.domain.user.repository.UserRepository;
import com.example.moneylog.global.common.ApiResponse;
import com.example.moneylog.global.exception.CustomException;
import com.example.moneylog.global.exception.ErrorCode;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public ApiResponse<List<CategoryResponse>> list(@AuthenticationPrincipal Long userId) {
        return ApiResponse.success("카테고리 목록을 조회했습니다.", categoryService.getMyCategories(currentUser(userId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> create(@AuthenticationPrincipal Long userId,
                                                                @Valid @RequestBody CategoryRequest req) {
        CategoryResponse created = categoryService.create(currentUser(userId), req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("카테고리가 등록되었습니다.", created));
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> update(@AuthenticationPrincipal Long userId,
                                                @PathVariable Long id,
                                                @Valid @RequestBody CategoryRequest req) {
        return ApiResponse.success("카테고리가 수정되었습니다.", categoryService.update(currentUser(userId), id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        categoryService.delete(currentUser(userId), id);
        return ApiResponse.success("카테고리가 삭제되었습니다.", null);
    }
}