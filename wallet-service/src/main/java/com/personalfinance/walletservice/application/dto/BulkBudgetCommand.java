package com.personalfinance.walletservice.application.dto;

import com.personalfinance.walletservice.domain.model.BudgetType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record BulkBudgetCommand(
        BigDecimal monthlyIncome,
        List<Allocation> allocations
) {
    public record Allocation(
            UUID categoryId,
            BudgetType budgetType,
            BigDecimal amount
    ) {}
}

