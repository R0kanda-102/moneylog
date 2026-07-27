package com.example.moneylog.domain.statistics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.moneylog.domain.category.entity.CategoryType;
import com.example.moneylog.domain.statistics.dto.StatisticsResponse;
import com.example.moneylog.domain.statistics.repository.StatisticsRepository;
import com.example.moneylog.domain.user.entity.User;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    StatisticsRepository statisticsRepository;

    @InjectMocks
    StatisticsService statisticsService;

    private User newUser() {
        User u = User.builder().email("test@moneylog.com").password("encoded-pw").nickname("테스터").build();
        ReflectionTestUtils.setField(u, "id", 1L);
        return u;
    }

    @Test
    void monthly_aggregatesIncomeExpenseAndBalance() {
        User user = newUser();
        LocalDate start = LocalDate.of(2026, 7, 1);
        LocalDate end = LocalDate.of(2026, 7, 31);
        when(statisticsRepository.sumByType(user, start, end)).thenReturn(List.of(
                new Object[]{CategoryType.INCOME, 2500000L},
                new Object[]{CategoryType.EXPENSE, 830000L}
        ));
        when(statisticsRepository.sumExpenseByCategory(user, start, end)).thenReturn(List.of(
                new StatisticsResponse.CategorySum("식비", 420000L),
                new StatisticsResponse.CategorySum("교통", 180000L)
        ));

        StatisticsResponse response = statisticsService.monthly(user, "2026-07");

        assertThat(response.income()).isEqualTo(2500000L);
        assertThat(response.expense()).isEqualTo(830000L);
        assertThat(response.balance()).isEqualTo(1670000L);
        assertThat(response.byCategory()).hasSize(2);
        assertThat(response.byCategory().get(0).categoryName()).isEqualTo("식비");
    }

    @Test
    void monthly_noTransactions_returnsAllZero() {
        User user = newUser();
        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 31);
        when(statisticsRepository.sumByType(user, start, end)).thenReturn(List.of());
        when(statisticsRepository.sumExpenseByCategory(user, start, end)).thenReturn(List.of());

        StatisticsResponse response = statisticsService.monthly(user, "2026-08");

        assertThat(response.income()).isZero();
        assertThat(response.expense()).isZero();
        assertThat(response.balance()).isZero();
        assertThat(response.byCategory()).isEmpty();
    }
}
