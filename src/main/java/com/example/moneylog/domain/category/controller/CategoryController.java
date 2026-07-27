package com.example.moneylog.domain.category.controller;

import com.example.moneylog.domain.category.dto.CategoryRequest;
import com.example.moneylog.domain.category.dto.CategoryResponse;
import com.example.moneylog.domain.category.service.CategoryService;
import com.example.moneylog.domain.user.entity.User;
import com.example.moneylog.domain.user.repository.UserRepository;
import com.example.moneylog.global.common.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final UserRepository userRepository;

    // TODO(3일차): @AuthenticationPrincipal로 로그인 사용자 교체
    private static final Long TEMP_USER_ID = 1L;

    private User currentUser() {
        return userRepository.findById(TEMP_USER_ID)
                .orElseThrow(() -> new IllegalStateException("임시 사용자가 없습니다. 먼저 사용자를 등록하세요."));
    }

    @GetMapping
    public ApiResponse<List<CategoryResponse>> list() {
        return ApiResponse.success("카테고리 목록을 조회했습니다.", categoryService.getMyCategories(currentUser()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> create(@Valid @RequestBody CategoryRequest req) {
        CategoryResponse created = categoryService.create(currentUser(), req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("카테고리가 등록되었습니다.", created));
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> update(@PathVariable Long id, @Valid @RequestBody CategoryRequest req) {
        return ApiResponse.success("카테고리가 수정되었습니다.", categoryService.update(currentUser(), id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        categoryService.delete(currentUser(), id);
        return ApiResponse.success("카테고리가 삭제되었습니다.", null);
    }
}