package com.personalfinance.transactionservice.application.service;

import com.personalfinance.transactionservice.application.dto.CreateTransactionCommand;
import com.personalfinance.transactionservice.application.dto.TransactionDto;
import com.personalfinance.transactionservice.application.dto.TransactionFilterCommand;
import com.personalfinance.transactionservice.application.dto.TransactionPageDto;
import com.personalfinance.transactionservice.application.dto.UpdateTransactionCommand;
import com.personalfinance.transactionservice.domain.exception.CategoryNotFoundException;
import com.personalfinance.transactionservice.domain.exception.TransactionNotFoundException;
import com.personalfinance.transactionservice.domain.model.Category;
import com.personalfinance.transactionservice.domain.model.Transaction;
import com.personalfinance.transactionservice.domain.model.TransactionType;
import com.personalfinance.transactionservice.domain.port.CategoryRepository;
import com.personalfinance.transactionservice.domain.port.TransactionRepository;
import com.personalfinance.transactionservice.infrastructure.messaging.publisher.TransactionEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionApplicationServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionEventPublisher eventPublisher;

    private TransactionApplicationService service;

    private final UUID userId = UUID.randomUUID();
    private final UUID walletId = UUID.randomUUID();
    private final UUID categoryId = UUID.randomUUID();
    private final UUID subCategoryId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new TransactionApplicationService(transactionRepository, categoryRepository, eventPublisher);
    }

    private Category topLevelCategory() {
        return new Category(categoryId, null, "Food", null, TransactionType.EXPENSE, OffsetDateTime.now());
    }

    private Category subCategory() {
        return new Category(subCategoryId, null, "Groceries", categoryId, TransactionType.EXPENSE, OffsetDateTime.now());
    }

    @Test
    void createTransactionWithValidCategories() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(topLevelCategory()));
        when(categoryRepository.findById(subCategoryId)).thenReturn(Optional.of(subCategory()));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateTransactionCommand command = new CreateTransactionCommand(
                walletId, TransactionType.EXPENSE, BigDecimal.TEN, "USD",
                categoryId, subCategoryId, "Groceries run", null);

        TransactionDto result = service.create(userId, command);

        assertThat(result.categoryId()).isEqualTo(categoryId);
        assertThat(result.subCategoryId()).isEqualTo(subCategoryId);
        assertThat(result.categoryName()).isEqualTo("Food");
        assertThat(result.subCategoryName()).isEqualTo("Groceries");
        verify(eventPublisher).publishCreated(any());
    }

    @Test
    void createTransactionWithNullCategories() {
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateTransactionCommand command = new CreateTransactionCommand(
                walletId, TransactionType.EXPENSE, BigDecimal.TEN, "USD",
                null, null, "No category", null);

        TransactionDto result = service.create(userId, command);

        assertThat(result.categoryId()).isNull();
        assertThat(result.subCategoryId()).isNull();
    }

    @Test
    void createTransactionWithInvalidCategoryThrows() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        CreateTransactionCommand command = new CreateTransactionCommand(
                walletId, TransactionType.EXPENSE, BigDecimal.TEN, "USD",
                categoryId, null, "desc", null);

        assertThatThrownBy(() -> service.create(userId, command))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void createTransactionWithNonTopLevelCategoryThrows() {
        Category sub = subCategory();
        when(categoryRepository.findById(subCategoryId)).thenReturn(Optional.of(sub));

        CreateTransactionCommand command = new CreateTransactionCommand(
                walletId, TransactionType.EXPENSE, BigDecimal.TEN, "USD",
                subCategoryId, null, "desc", null);

        assertThatThrownBy(() -> service.create(userId, command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("top-level");
    }

    @Test
    void createTransactionWithMismatchedSubcategoryThrows() {
        UUID otherParentId = UUID.randomUUID();
        Category wrongSub = new Category(subCategoryId, null, "Other", otherParentId, TransactionType.EXPENSE, OffsetDateTime.now());
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(topLevelCategory()));
        when(categoryRepository.findById(subCategoryId)).thenReturn(Optional.of(wrongSub));

        CreateTransactionCommand command = new CreateTransactionCommand(
                walletId, TransactionType.EXPENSE, BigDecimal.TEN, "USD",
                categoryId, subCategoryId, "desc", null);

        assertThatThrownBy(() -> service.create(userId, command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not belong");
    }

    @Test
    void getByIdReturnsTransaction() {
        Transaction transaction = Transaction.create(userId, walletId, TransactionType.EXPENSE,
                BigDecimal.TEN, "USD", categoryId, null, "desc", null);
        when(transactionRepository.findByIdAndUserId(transaction.getId(), userId)).thenReturn(Optional.of(transaction));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(topLevelCategory()));

        TransactionDto result = service.getById(userId, transaction.getId());

        assertThat(result.id()).isEqualTo(transaction.getId());
        assertThat(result.categoryName()).isEqualTo("Food");
    }

    @Test
    void getByIdNotFoundThrows() {
        UUID txId = UUID.randomUUID();
        when(transactionRepository.findByIdAndUserId(txId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(userId, txId))
                .isInstanceOf(TransactionNotFoundException.class);
    }

    @Test
    void listByWallet() {
        Pageable pageable = PageRequest.of(0, 10);
        Transaction tx = Transaction.create(userId, walletId, TransactionType.EXPENSE,
                BigDecimal.TEN, "USD", null, null, "desc", null);
        Page<Transaction> page = new PageImpl<>(List.of(tx), pageable, 1);
        when(transactionRepository.findByWalletIdAndUserId(walletId, userId, pageable)).thenReturn(page);

        TransactionPageDto result = service.listByWallet(userId, walletId, pageable);

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void listByUser() {
        Pageable pageable = PageRequest.of(0, 10);
        Transaction tx = Transaction.create(userId, walletId, TransactionType.EXPENSE,
                BigDecimal.TEN, "USD", null, null, "desc", null);
        Page<Transaction> page = new PageImpl<>(List.of(tx), pageable, 1);
        TransactionFilterCommand filter = new TransactionFilterCommand(userId, null, null, null, null, null, pageable);
        when(transactionRepository.findByUserId(userId, null, null, null, null, pageable)).thenReturn(page);

        TransactionPageDto result = service.listByUser(filter);

        assertThat(result.content()).hasSize(1);
    }

    @Test
    void updateTransactionWithValidCategories() {
        Transaction existing = Transaction.create(userId, walletId, TransactionType.EXPENSE,
                BigDecimal.TEN, "USD", null, null, "old desc", null);
        when(transactionRepository.findByIdAndUserId(existing.getId(), userId)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(topLevelCategory()));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateTransactionCommand command = new UpdateTransactionCommand(
                TransactionType.EXPENSE, BigDecimal.valueOf(20), categoryId, null, "new desc", null);

        TransactionDto result = service.update(userId, existing.getId(), command);

        assertThat(result.categoryId()).isEqualTo(categoryId);
        assertThat(result.amount()).isEqualByComparingTo(BigDecimal.valueOf(20));
    }

    @Test
    void deleteTransactionPublishesEvent() {
        Transaction tx = Transaction.create(userId, walletId, TransactionType.EXPENSE,
                BigDecimal.TEN, "USD", null, null, "desc", null);
        when(transactionRepository.findByIdAndUserId(tx.getId(), userId)).thenReturn(Optional.of(tx));

        service.delete(userId, tx.getId());

        verify(transactionRepository).delete(tx);
        verify(eventPublisher).publishDeleted(any());
    }
}

