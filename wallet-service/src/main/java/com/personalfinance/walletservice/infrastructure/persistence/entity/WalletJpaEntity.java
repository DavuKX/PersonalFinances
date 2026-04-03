package com.personalfinance.walletservice.infrastructure.persistence.entity;

import com.personalfinance.walletservice.domain.model.LimitPeriod;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallets", indexes = {
        @Index(name = "idx_wallets_user_id", columnList = "userId"),
        @Index(name = "idx_wallets_archived", columnList = "archived")
})
public class WalletJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Column(precision = 19, scale = 4)
    private BigDecimal spendingLimitAmount;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private LimitPeriod spendingLimitPeriod;

    @Column(precision = 19, scale = 4)
    private BigDecimal monthlyIncome;

    @Column(nullable = false)
    private boolean archived = false;

    private OffsetDateTime archivedAt;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public BigDecimal getSpendingLimitAmount() { return spendingLimitAmount; }
    public void setSpendingLimitAmount(BigDecimal spendingLimitAmount) { this.spendingLimitAmount = spendingLimitAmount; }

    public LimitPeriod getSpendingLimitPeriod() { return spendingLimitPeriod; }
    public void setSpendingLimitPeriod(LimitPeriod spendingLimitPeriod) { this.spendingLimitPeriod = spendingLimitPeriod; }

    public BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }

    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }

    public OffsetDateTime getArchivedAt() { return archivedAt; }
    public void setArchivedAt(OffsetDateTime archivedAt) { this.archivedAt = archivedAt; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
