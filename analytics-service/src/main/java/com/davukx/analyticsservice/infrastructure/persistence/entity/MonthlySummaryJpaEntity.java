package com.davukx.analyticsservice.infrastructure.persistence.entity;

import com.davukx.analyticsservice.domain.model.TransactionType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "monthly_summaries", indexes = {
        @Index(name = "idx_ms_user_wallet_period", columnList = "userId, walletId, year, month"),
        @Index(name = "idx_ms_user_period", columnList = "userId, year, month")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uq_ms_user_wallet_year_month", columnNames = {"userId", "walletId", "year", "month"})
})
public class MonthlySummaryJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID walletId;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false)
    private int month;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal totalIncome;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal totalExpenses;

    @Column(nullable = false, precision = 19, scale = 4, columnDefinition = "numeric(19,4) default 0")
    private BigDecimal totalSavings = BigDecimal.ZERO;

    @Column(nullable = false)
    private int transactionCount;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getWalletId() { return walletId; }
    public void setWalletId(UUID walletId) { this.walletId = walletId; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }

    public BigDecimal getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(BigDecimal totalExpenses) { this.totalExpenses = totalExpenses; }

    public BigDecimal getTotalSavings() { return totalSavings; }
    public void setTotalSavings(BigDecimal totalSavings) { this.totalSavings = totalSavings; }

    public int getTransactionCount() { return transactionCount; }
    public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}

