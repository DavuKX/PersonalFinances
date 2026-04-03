package com.personalfinance.transactionservice.application.service;

import com.personalfinance.transactionservice.application.dto.CreateTransactionCommand;
import com.personalfinance.transactionservice.application.dto.SpendingSummaryDto;
import com.personalfinance.transactionservice.application.dto.TransactionDto;
import com.personalfinance.transactionservice.application.dto.TransactionFilterCommand;
import com.personalfinance.transactionservice.application.dto.TransactionPageDto;
import com.personalfinance.transactionservice.application.dto.UpdateTransactionCommand;
import com.personalfinance.transactionservice.application.usecase.TransactionUseCase;
import com.personalfinance.transactionservice.domain.exception.CategoryNotFoundException;
import com.personalfinance.transactionservice.domain.exception.TransactionNotFoundException;
import com.personalfinance.transactionservice.domain.model.Category;
import com.personalfinance.transactionservice.domain.model.Transaction;
import com.personalfinance.transactionservice.domain.port.CategoryRepository;
import com.personalfinance.transactionservice.domain.port.TransactionRepository;
import com.personalfinance.transactionservice.infrastructure.messaging.event.TransactionCreatedEvent;
import com.personalfinance.transactionservice.infrastructure.messaging.event.TransactionDeletedEvent;
import com.personalfinance.transactionservice.infrastructure.messaging.publisher.TransactionEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class TransactionApplicationService implements TransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionEventPublisher eventPublisher;

    public TransactionApplicationService(TransactionRepository transactionRepository,
                                         CategoryRepository categoryRepository,
                                         TransactionEventPublisher eventPublisher) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public TransactionDto create(UUID userId, CreateTransactionCommand command) {
        validateCategories(userId, command.categoryId(), command.subCategoryId());
        Transaction transaction = Transaction.create(userId, command.walletId(), command.type(),
                command.amount(), command.currency(), command.categoryId(), command.subCategoryId(),
                command.description(), command.transactionDate());
        Transaction saved = transactionRepository.save(transaction);
        eventPublisher.publishCreated(new TransactionCreatedEvent(
                saved.getId(), saved.getUserId(), saved.getWalletId(),
                saved.getType(), saved.getAmount(), saved.getCurrency(),
                saved.getCategoryId(), saved.getSubCategoryId(), saved.getTransactionDate()));
        return toDto(saved);
    }

    @Override
    public TransactionDto getById(UUID userId, UUID transactionId) {
        return toDto(loadTransaction(userId, transactionId));
    }

    @Override
    public TransactionPageDto listByWallet(UUID userId, UUID walletId, Pageable pageable) {
        return toPageDto(transactionRepository.findByWalletIdAndUserId(walletId, userId, pageable));
    }

    @Override
    public TransactionPageDto listByUser(TransactionFilterCommand filter) {
        return toPageDto(transactionRepository.findByUserId(
                filter.userId(), filter.type(), filter.categoryId(), filter.from(), filter.to(), filter.pageable()));
    }

    @Override
    @Transactional
    public TransactionDto update(UUID userId, UUID transactionId, UpdateTransactionCommand command) {
        Transaction transaction = loadTransaction(userId, transactionId);
        validateCategories(userId, command.categoryId(), command.subCategoryId());
        Transaction updated = transaction.withDetails(command.type(), command.amount(), command.categoryId(),
                command.subCategoryId(), command.description(), command.transactionDate());
        return toDto(transactionRepository.save(updated));
    }

    @Override
    @Transactional
    public void delete(UUID userId, UUID transactionId) {
        Transaction transaction = loadTransaction(userId, transactionId);
        transactionRepository.delete(transaction);
        eventPublisher.publishDeleted(new TransactionDeletedEvent(
                transaction.getId(), transaction.getUserId(), transaction.getWalletId(),
                transaction.getType(), transaction.getAmount(), transaction.getCurrency(),
                transaction.getCategoryId(), transaction.getSubCategoryId(), transaction.getTransactionDate()));
    }

    @Override
    public SpendingSummaryDto getSpendingSummary(UUID userId, UUID walletId, OffsetDateTime from, OffsetDateTime to) {
        java.math.BigDecimal spent = transactionRepository.sumSpending(walletId, userId, from, to);
        return new SpendingSummaryDto(spent, from, to);
    }

    private void validateCategories(UUID userId, UUID categoryId, UUID subCategoryId) {
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
            if (!category.isAccessibleBy(userId)) {
                throw new CategoryNotFoundException("Category not found");
            }
            if (!category.isTopLevel()) {
                throw new IllegalArgumentException("categoryId must reference a top-level category");
            }
        }
        if (subCategoryId != null) {
            Category subCategory = categoryRepository.findById(subCategoryId)
                    .orElseThrow(() -> new CategoryNotFoundException("Subcategory not found"));
            if (!subCategory.isAccessibleBy(userId)) {
                throw new CategoryNotFoundException("Subcategory not found");
            }
            if (categoryId != null && !categoryId.equals(subCategory.getParentId())) {
                throw new IllegalArgumentException("Subcategory does not belong to the specified category");
            }
        }
    }

    private Transaction loadTransaction(UUID userId, UUID transactionId) {
        return transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));
    }

    private TransactionPageDto toPageDto(Page<Transaction> page) {
        return new TransactionPageDto(
                page.getContent().stream().map(this::toDto).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    private TransactionDto toDto(Transaction t) {
        String categoryName = resolveCategoryName(t.getCategoryId());
        String subCategoryName = resolveCategoryName(t.getSubCategoryId());
        return new TransactionDto(t.getId(), t.getUserId(), t.getWalletId(), t.getType(), t.getAmount(),
                t.getCurrency(), t.getCategoryId(), t.getSubCategoryId(), categoryName, subCategoryName,
                t.getDescription(), t.getTransactionDate(), t.getCreatedAt(), t.getUpdatedAt());
    }

    private String resolveCategoryName(UUID categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId).map(Category::getName).orElse(null);
    }
}
