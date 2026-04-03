package com.personalfinance.walletservice.presentation.request;

import com.personalfinance.walletservice.domain.model.BudgetType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public class CreateBudgetRequest {

    @NotNull
    private UUID categoryId;

    @NotNull
    private BudgetType budgetType;

    @NotNull
    @Positive
    private BigDecimal amount;

    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }

    public BudgetType getBudgetType() { return budgetType; }
    public void setBudgetType(BudgetType budgetType) { this.budgetType = budgetType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}

