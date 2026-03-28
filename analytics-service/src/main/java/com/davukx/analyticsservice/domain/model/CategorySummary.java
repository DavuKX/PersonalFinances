package com.davukx.analyticsservice.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class CategorySummary {

    private final UUID id;
    private final UUID userId;
    private final UUID walletId;
    private final UUID categoryId;
    private final int year;
    private final int month;
    private final TransactionType transactionType;
    private BigDecimal totalAmount;
    private int transactionCount;
    private OffsetDateTime updatedAt;

    public CategorySummary(UUID id, UUID userId, UUID walletId, UUID categoryId,
                           int year, int month, TransactionType transactionType,
                           BigDecimal totalAmount, int transactionCount, OffsetDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.walletId = walletId;
        this.categoryId = categoryId;
        this.year = year;
        this.month = month;
        this.transactionType = transactionType;
        this.totalAmount = totalAmount;
        this.transactionCount = transactionCount;
        this.updatedAt = updatedAt;
    }

    public static CategorySummary empty(UUID userId, UUID walletId, UUID categoryId,
                                        int year, int month, TransactionType type) {
        return new CategorySummary(UUID.randomUUID(), userId, walletId, categoryId,
                year, month, type, BigDecimal.ZERO, 0, OffsetDateTime.now());
    }

    public CategorySummary applyCreated(BigDecimal amount) {
        totalAmount = totalAmount.add(amount);
        transactionCount++;
        updatedAt = OffsetDateTime.now();
        return this;
    }

    public CategorySummary applyDeleted(BigDecimal amount) {
        totalAmount = totalAmount.subtract(amount).max(BigDecimal.ZERO);
        transactionCount = Math.max(0, transactionCount - 1);
        updatedAt = OffsetDateTime.now();
        return this;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getWalletId() { return walletId; }
    public UUID getCategoryId() { return categoryId; }
    public int getYear() { return year; }
    public int getMonth() { return month; }
    public TransactionType getTransactionType() { return transactionType; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public int getTransactionCount() { return transactionCount; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}

