package com.personalfinance.walletservice.presentation.response;

import com.personalfinance.walletservice.domain.model.LimitPeriod;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class WalletResponse {

    private UUID id;
    private UUID userId;
    private String name;
    private String currency;
    private BigDecimal balance;
    private BigDecimal spendingLimitAmount;
    private LimitPeriod spendingLimitPeriod;
    private boolean archived;
    private OffsetDateTime archivedAt;
    private OffsetDateTime createdAt;
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

    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }

    public OffsetDateTime getArchivedAt() { return archivedAt; }
    public void setArchivedAt(OffsetDateTime archivedAt) { this.archivedAt = archivedAt; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
