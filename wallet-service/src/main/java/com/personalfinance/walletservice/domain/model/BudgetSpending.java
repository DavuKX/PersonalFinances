package com.personalfinance.walletservice.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public class BudgetSpending {

    private final UUID id;
    private final UUID walletId;
    private final UUID userId;
    private final UUID categoryId;
    private final LocalDate periodStart;
    private final LocalDate periodEnd;
    private final BigDecimal totalSpent;
    private final OffsetDateTime updatedAt;

    public BudgetSpending(UUID id, UUID walletId, UUID userId, UUID categoryId,
                          LocalDate periodStart, LocalDate periodEnd,
                          BigDecimal totalSpent, OffsetDateTime updatedAt) {
        this.id = id;
        this.walletId = walletId;
        this.userId = userId;
        this.categoryId = categoryId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.totalSpent = totalSpent;
        this.updatedAt = updatedAt;
    }

    public static BudgetSpending create(UUID walletId, UUID userId, UUID categoryId,
                                        LocalDate periodStart, LocalDate periodEnd) {
        return new BudgetSpending(UUID.randomUUID(), walletId, userId, categoryId,
                periodStart, periodEnd, BigDecimal.ZERO, OffsetDateTime.now());
    }

    public BudgetSpending addSpending(BigDecimal amount) {
        return new BudgetSpending(id, walletId, userId, categoryId, periodStart, periodEnd,
                totalSpent.add(amount), OffsetDateTime.now());
    }

    public BudgetSpending subtractSpending(BigDecimal amount) {
        BigDecimal newTotal = totalSpent.subtract(amount);
        if (newTotal.compareTo(BigDecimal.ZERO) < 0) newTotal = BigDecimal.ZERO;
        return new BudgetSpending(id, walletId, userId, categoryId, periodStart, periodEnd,
                newTotal, OffsetDateTime.now());
    }

    public UUID getId() { return id; }
    public UUID getWalletId() { return walletId; }
    public UUID getUserId() { return userId; }
    public UUID getCategoryId() { return categoryId; }
    public LocalDate getPeriodStart() { return periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public BigDecimal getTotalSpent() { return totalSpent; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}

