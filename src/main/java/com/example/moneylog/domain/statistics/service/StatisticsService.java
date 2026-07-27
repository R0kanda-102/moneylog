package com.example.moneylog.domain.statistics.service;

import com.example.moneylog.domain.category.entity.CategoryType;
import com.example.moneylog.domain.statistics.dto.StatisticsResponse;
import com.example.moneylog.domain.statistics.repository.StatisticsRepository;
import com.example.moneylog.domain.user.entity.User;
import java.time.LocalDate;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final StatisticsRepository statisticsRepository;

    @Transactional(readOnly = true)
    public StatisticsResponse monthly(User user, String yearMonth) {
        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        long income = 0, expense = 0;
        for (Object[] row : statisticsRepository.sumByType(user, start, end)) {
            CategoryType type = (CategoryType) row[0];
            long sum = (long) row[1];
            if (type == CategoryType.INCOME) income = sum;
            else expense = sum;
        }

        var byCategory = statisticsRepository.sumExpenseByCategory(user, start, end);
        return StatisticsResponse.of(income, expense, byCategory);
    }
}