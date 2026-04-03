package com.personalfinance.walletservice.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class Budget {

    private final UUID id;
    private final UUID walletId;
    private final UUID userId;
    private final UUID categoryId;
    private final BudgetType budgetType;
    private final BigDecimal amount;
    private final BudgetPeriod period;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    public Budget(UUID id, UUID walletId, UUID userId, UUID categoryId,
                  BudgetType budgetType, BigDecimal amount, BudgetPeriod period,
                  OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.walletId = walletId;
        this.userId = userId;
        this.categoryId = categoryId;
        this.budgetType = budgetType;
        this.amount = amount;
        this.period = period;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Budget create(UUID walletId, UUID userId, UUID categoryId,
                                BudgetType budgetType, BigDecimal amount) {
        OffsetDateTime now = OffsetDateTime.now();
        return new Budget(UUID.randomUUID(), walletId, userId, categoryId,
                budgetType, amount, BudgetPeriod.MONTHLY, now, now);
    }

    public Budget withAmount(BudgetType budgetType, BigDecimal amount) {
        return new Budget(id, walletId, userId, categoryId, budgetType, amount, period, createdAt, OffsetDateTime.now());
    }

    public boolean belongsTo(UUID ownerId) {
        return userId.equals(ownerId);
    }

    public UUID getId() { return id; }
    public UUID getWalletId() { return walletId; }
    public UUID getUserId() { return userId; }
    public UUID getCategoryId() { return categoryId; }
    public BudgetType getBudgetType() { return budgetType; }
    public BigDecimal getAmount() { return amount; }
    public BudgetPeriod getPeriod() { return period; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}

