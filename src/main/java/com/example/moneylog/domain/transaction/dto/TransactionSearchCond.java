package com.example.moneylog.domain.transaction.dto;

import com.example.moneylog.domain.category.entity.CategoryType;

public record TransactionSearchCond(
        String yearMonth,
        CategoryType type,
        Long categoryId
) {}