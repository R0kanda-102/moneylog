package com.example.moneylog.domain.transaction.repository;

import com.example.moneylog.domain.category.entity.CategoryType;
import com.example.moneylog.domain.transaction.entity.Transaction;
import com.example.moneylog.domain.user.entity.User;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByIdAndUser(Long id, User user);

    @Query("""
            SELECT t FROM Transaction t
            JOIN FETCH t.category c
            WHERE t.user = :user
              AND t.transactionDate BETWEEN :start AND :end
              AND (:type IS NULL OR t.type = :type)
              AND (:categoryId IS NULL OR c.id = :categoryId)
            """)
    Page<Transaction> search(@Param("user") User user,
                             @Param("start") LocalDate start,
                             @Param("end") LocalDate end,
                             @Param("type") CategoryType type,
                             @Param("categoryId") Long categoryId,
                             Pageable pageable);
}