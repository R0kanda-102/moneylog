package com.example.moneylog.domain.transaction.dto;

import com.example.moneylog.domain.category.entity.CategoryType;
import com.example.moneylog.domain.transaction.entity.Transaction;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        CategoryType type,
        Long amount,
        Long categoryId,
        String categoryName,
        String description,
        LocalDate transactionDate,
        LocalDateTime createdAt
) {
    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getType(),
                t.getAmount(),
                t.getCategory().getId(),
                t.getCategory().getName(),
                t.getDescription(),
                t.getTransactionDate(),
                t.getCreatedAt()
        );
    }
}