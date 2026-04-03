package com.personalfinance.walletservice.presentation.request;

import com.personalfinance.walletservice.domain.model.BudgetType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class UpdateBudgetRequest {

    @NotNull
    private BudgetType budgetType;

    @NotNull
    @Positive
    private BigDecimal amount;

    public BudgetType getBudgetType() { return budgetType; }
    public void setBudgetType(BudgetType budgetType) { this.budgetType = budgetType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}

