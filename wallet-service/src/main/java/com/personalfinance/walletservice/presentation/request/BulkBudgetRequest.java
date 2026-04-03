package com.personalfinance.walletservice.presentation.request;

import com.personalfinance.walletservice.domain.model.BudgetType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class BulkBudgetRequest {

    @Positive
    private BigDecimal monthlyIncome;

    @NotNull
    @Size(min = 1)
    @Valid
    private List<Allocation> allocations;

    public BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }

    public List<Allocation> getAllocations() { return allocations; }
    public void setAllocations(List<Allocation> allocations) { this.allocations = allocations; }

    public static class Allocation {
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
}

