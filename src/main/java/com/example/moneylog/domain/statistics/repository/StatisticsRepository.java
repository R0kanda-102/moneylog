package com.example.moneylog.domain.statistics.repository;

import com.example.moneylog.domain.category.entity.CategoryType;
import com.example.moneylog.domain.statistics.dto.StatisticsResponse;
import com.example.moneylog.domain.transaction.entity.Transaction;
import com.example.moneylog.domain.user.entity.User;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StatisticsRepository extends JpaRepository<Transaction, Long> {

    @Query("""
            SELECT t.type, COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE t.user = :user
              AND t.transactionDate BETWEEN :start AND :end
            GROUP BY t.type
            """)
    List<Object[]> sumByType(@Param("user") User user,
                             @Param("start") LocalDate start,
                             @Param("end") LocalDate end);

    @Query("""
            SELECT new com.example.moneylog.domain.statistics.dto.StatisticsResponse$CategorySum(c.name, SUM(t.amount))
            FROM Transaction t JOIN t.category c
            WHERE t.user = :user
              AND t.type = com.example.moneylog.domain.category.entity.CategoryType.EXPENSE
              AND t.transactionDate BETWEEN :start AND :end
            GROUP BY c.id, c.name
            ORDER BY SUM(t.amount) DESC
            """)
    List<StatisticsResponse.CategorySum> sumExpenseByCategory(@Param("user") User user,
                                                              @Param("start") LocalDate start,
                                                              @Param("end") LocalDate end);
}