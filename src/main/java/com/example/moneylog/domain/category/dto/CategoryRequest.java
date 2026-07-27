package com.example.moneylog.domain.category.dto;

import com.example.moneylog.domain.category.entity.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoryRequest(
        @NotBlank(message = "카테고리 이름은 필수입니다.")
        String name,

        @NotNull(message = "카테고리 타입은 필수입니다.")
        CategoryType type
) {}