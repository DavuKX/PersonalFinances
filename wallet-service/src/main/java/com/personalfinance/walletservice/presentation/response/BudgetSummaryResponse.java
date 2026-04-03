package com.personalfinance.walletservice.presentation.response;

import com.personalfinance.walletservice.domain.model.BudgetPeriod;
import com.personalfinance.walletservice.domain.model.BudgetType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class BudgetSummaryResponse {

    private UUID id;
    private UUID walletId;
    private UUID userId;
    private UUID categoryId;
    private BudgetType budgetType;
    private BigDecimal amount;
    private BigDecimal resolvedAmount;
    private BudgetPeriod period;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private double percentUsed;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getWalletId() { return walletId; }
    public void setWalletId(UUID walletId) { this.walletId = walletId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }

    public BudgetType getBudgetType() { return budgetType; }
    public void setBudgetType(BudgetType budgetType) { this.budgetType = budgetType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getResolvedAmount() { return resolvedAmount; }
    public void setResolvedAmount(BigDecimal resolvedAmount) { this.resolvedAmount = resolvedAmount; }

    public BudgetPeriod getPeriod() { return period; }
    public void setPeriod(BudgetPeriod period) { this.period = period; }

    public BigDecimal getSpentAmount() { return spentAmount; }
    public void setSpentAmount(BigDecimal spentAmount) { this.spentAmount = spentAmount; }

    public BigDecimal getRemainingAmount() { return remainingAmount; }
    public void setRemainingAmount(BigDecimal remainingAmount) { this.remainingAmount = remainingAmount; }

    public double getPercentUsed() { return percentUsed; }
    public void setPercentUsed(double percentUsed) { this.percentUsed = percentUsed; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}

