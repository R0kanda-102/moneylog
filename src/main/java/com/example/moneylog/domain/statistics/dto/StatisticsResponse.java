package com.example.moneylog.domain.statistics.dto;

import java.util.List;

public record StatisticsResponse(
        long income,
        long expense,
        long balance,
        List<CategorySum> byCategory
) {
    public record CategorySum(String categoryName, long total) {}

    public static StatisticsResponse of(long income, long expense, List<CategorySum> byCategory) {
        return new StatisticsResponse(income, expense, income - expense, byCategory);
    }
}