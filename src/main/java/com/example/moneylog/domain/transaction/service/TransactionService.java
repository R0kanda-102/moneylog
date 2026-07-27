package com.example.moneylog.domain.transaction.service;

import com.example.moneylog.domain.category.entity.Category;
import com.example.moneylog.domain.category.repository.CategoryRepository;
import com.example.moneylog.domain.transaction.dto.TransactionRequest;
import com.example.moneylog.domain.transaction.dto.TransactionResponse;
import com.example.moneylog.domain.transaction.dto.TransactionSearchCond;
import com.example.moneylog.domain.transaction.entity.Transaction;
import com.example.moneylog.domain.transaction.repository.TransactionRepository;
import com.example.moneylog.domain.user.entity.User;
import com.example.moneylog.global.exception.CustomException;
import com.example.moneylog.global.exception.ErrorCode;
import java.time.LocalDate;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public TransactionResponse create(User user, TransactionRequest req) {
        Category category = categoryRepository.findByIdAndUser(req.categoryId(), user)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        Transaction tx = Transaction.builder()
                .user(user)
                .category(category)
                .type(req.type())
                .amount(req.amount())
                .description(req.description())
                .transactionDate(req.transactionDate())
                .build();

        return TransactionResponse.from(transactionRepository.save(tx));
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getList(User user, TransactionSearchCond cond, Pageable pageable) {
        YearMonth ym = YearMonth.parse(cond.yearMonth());
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        Page<Transaction> page = transactionRepository.search(
                user, start, end, cond.type(), cond.categoryId(), pageable);

        return page.map(TransactionResponse::from);
    }

    @Transactional(readOnly = true)
    public TransactionResponse get(User user, Long id) {
        return TransactionResponse.from(findOwned(user, id));
    }

    @Transactional
    public TransactionResponse update(User user, Long id, TransactionRequest req) {
        Transaction tx = findOwned(user, id);
        Category category = categoryRepository.findByIdAndUser(req.categoryId(), user)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
        tx.update(category, req.type(), req.amount(), req.description(), req.transactionDate());
        return TransactionResponse.from(tx);
    }

    @Transactional
    public void delete(User user, Long id) {
        Transaction tx = findOwned(user, id);
        transactionRepository.delete(tx);
    }

    private Transaction findOwned(User user, Long id) {
        return transactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));
    }
}