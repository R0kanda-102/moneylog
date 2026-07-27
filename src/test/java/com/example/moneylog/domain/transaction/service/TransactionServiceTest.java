package com.example.moneylog.domain.transaction.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.moneylog.domain.category.entity.Category;
import com.example.moneylog.domain.category.entity.CategoryType;
import com.example.moneylog.domain.category.repository.CategoryRepository;
import com.example.moneylog.domain.transaction.dto.TransactionRequest;
import com.example.moneylog.domain.transaction.dto.TransactionResponse;
import com.example.moneylog.domain.transaction.entity.Transaction;
import com.example.moneylog.domain.transaction.repository.TransactionRepository;
import com.example.moneylog.domain.user.entity.User;
import com.example.moneylog.global.exception.CustomException;
import com.example.moneylog.global.exception.ErrorCode;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    TransactionRepository transactionRepository;
    @Mock
    CategoryRepository categoryRepository;

    @InjectMocks
    TransactionService transactionService;

    private User newUser() {
        User u = User.builder().email("test@moneylog.com").password("encoded-pw").nickname("테스터").build();
        ReflectionTestUtils.setField(u, "id", 1L);
        return u;
    }

    private Category newCategory(User user, Long id) {
        Category category = Category.builder().user(user).name("식비").type(CategoryType.EXPENSE).build();
        ReflectionTestUtils.setField(category, "id", id);
        return category;
    }

    @Test
    void create_success_savesTransaction() {
        User user = newUser();
        Category category = newCategory(user, 3L);
        TransactionRequest req = new TransactionRequest(CategoryType.EXPENSE, 12000L, 3L, "점심", LocalDate.of(2026, 7, 8));
        when(categoryRepository.findByIdAndUser(3L, user)).thenReturn(Optional.of(category));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            ReflectionTestUtils.setField(tx, "id", 42L);
            return tx;
        });

        TransactionResponse response = transactionService.create(user, req);

        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.amount()).isEqualTo(12000L);
        assertThat(response.categoryName()).isEqualTo("식비");
    }

    @Test
    void create_categoryNotOwned_throwsCategoryNotFound() {
        User user = newUser();
        TransactionRequest req = new TransactionRequest(CategoryType.EXPENSE, 12000L, 99L, "점심", LocalDate.of(2026, 7, 8));
        when(categoryRepository.findByIdAndUser(eq(99L), eq(user))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.create(user, req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void get_ownedTransaction_returnsIt() {
        User user = newUser();
        Category category = newCategory(user, 3L);
        Transaction tx = Transaction.builder()
                .user(user).category(category).type(CategoryType.EXPENSE)
                .amount(12000L).description("점심").transactionDate(LocalDate.of(2026, 7, 8))
                .build();
        when(transactionRepository.findByIdAndUser(42L, user)).thenReturn(Optional.of(tx));

        TransactionResponse response = transactionService.get(user, 42L);

        assertThat(response.amount()).isEqualTo(12000L);
    }

    @Test
    void get_notOwnedTransaction_throwsTransactionNotFound() {
        User user = newUser();
        when(transactionRepository.findByIdAndUser(eq(42L), eq(user))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.get(user, 42L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.TRANSACTION_NOT_FOUND);
    }

    @Test
    void update_ownedTransaction_updatesFields() {
        User user = newUser();
        Category oldCategory = newCategory(user, 3L);
        Category newCategory = newCategory(user, 4L);
        Transaction tx = Transaction.builder()
                .user(user).category(oldCategory).type(CategoryType.EXPENSE)
                .amount(12000L).description("점심").transactionDate(LocalDate.of(2026, 7, 8))
                .build();
        TransactionRequest req = new TransactionRequest(CategoryType.EXPENSE, 15000L, 4L, "저녁", LocalDate.of(2026, 7, 9));
        when(transactionRepository.findByIdAndUser(42L, user)).thenReturn(Optional.of(tx));
        when(categoryRepository.findByIdAndUser(4L, user)).thenReturn(Optional.of(newCategory));

        TransactionResponse response = transactionService.update(user, 42L, req);

        assertThat(response.amount()).isEqualTo(15000L);
        assertThat(response.categoryId()).isEqualTo(4L);
        assertThat(response.description()).isEqualTo("저녁");
    }

    @Test
    void delete_ownedTransaction_deletesIt() {
        User user = newUser();
        Category category = newCategory(user, 3L);
        Transaction tx = Transaction.builder()
                .user(user).category(category).type(CategoryType.EXPENSE)
                .amount(12000L).description("점심").transactionDate(LocalDate.of(2026, 7, 8))
                .build();
        when(transactionRepository.findByIdAndUser(42L, user)).thenReturn(Optional.of(tx));

        transactionService.delete(user, 42L);

        verify(transactionRepository).delete(tx);
    }
}
