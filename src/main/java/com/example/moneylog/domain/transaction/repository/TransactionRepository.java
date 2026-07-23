package com.example.moneylog.domain.transaction.repository;

import com.example.moneylog.domain.transaction.entity.Transaction;
import com.example.moneylog.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByIdAndUser(Long id, User user);
}