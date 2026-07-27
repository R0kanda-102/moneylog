package com.example.moneylog.domain.transaction.dto;

import com.example.moneylog.domain.category.entity.CategoryType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record TransactionRequest(
        @NotNull(message = "거래 타입은 필수입니다.")
        CategoryType type,

        @NotNull(message = "금액은 필수입니다.")
        @Positive(message = "금액은 0보다 커야 합니다.")
        Long amount,

        @NotNull(message = "카테고리는 필수입니다.")
        Long categoryId,

        @Size(max = 255, message = "설명은 255자 이하여야 합니다.")
        String description,

        @NotNull(message = "거래일은 필수입니다.")
        LocalDate transactionDate
) {}