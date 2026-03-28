package com.davukx.analyticsservice.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.UUID;

public class MonthlySummary {

    private final UUID id;
    private final UUID userId;
    private final UUID walletId;
    private final int year;
    private final int month;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netSavings;
    private BigDecimal savingsRate;
    private int transactionCount;
    private OffsetDateTime updatedAt;

    public MonthlySummary(UUID id, UUID userId, UUID walletId, int year, int month,
                          BigDecimal totalIncome, BigDecimal totalExpenses,
                          int transactionCount, OffsetDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.walletId = walletId;
        this.year = year;
        this.month = month;
        this.totalIncome = totalIncome;
        this.totalExpenses = totalExpenses;
        this.transactionCount = transactionCount;
        this.updatedAt = updatedAt;
        recalculateDerived();
    }

    public static MonthlySummary empty(UUID userId, UUID walletId, int year, int month) {
        return new MonthlySummary(UUID.randomUUID(), userId, walletId, year, month,
                BigDecimal.ZERO, BigDecimal.ZERO, 0, OffsetDateTime.now());
    }

    public MonthlySummary applyCreated(TransactionType type, BigDecimal amount) {
        if (type == TransactionType.INCOME) {
            totalIncome = totalIncome.add(amount);
        } else {
            totalExpenses = totalExpenses.add(amount);
        }
        transactionCount++;
        updatedAt = OffsetDateTime.now();
        recalculateDerived();
        return this;
    }

    public MonthlySummary applyDeleted(TransactionType type, BigDecimal amount) {
        if (type == TransactionType.INCOME) {
            totalIncome = totalIncome.subtract(amount).max(BigDecimal.ZERO);
        } else {
            totalExpenses = totalExpenses.subtract(amount).max(BigDecimal.ZERO);
        }
        transactionCount = Math.max(0, transactionCount - 1);
        updatedAt = OffsetDateTime.now();
        recalculateDerived();
        return this;
    }

    private void recalculateDerived() {
        netSavings = totalIncome.subtract(totalExpenses);
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            savingsRate = netSavings.divide(totalIncome, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
        } else {
            savingsRate = BigDecimal.ZERO;
        }
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getWalletId() { return walletId; }
    public int getYear() { return year; }
    public int getMonth() { return month; }
    public BigDecimal getTotalIncome() { return totalIncome; }
    public BigDecimal getTotalExpenses() { return totalExpenses; }
    public BigDecimal getNetSavings() { return netSavings; }
    public BigDecimal getSavingsRate() { return savingsRate; }
    public int getTransactionCount() { return transactionCount; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}

