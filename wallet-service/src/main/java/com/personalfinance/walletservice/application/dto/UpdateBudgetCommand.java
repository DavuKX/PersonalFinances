package com.personalfinance.walletservice.application.dto;

import com.personalfinance.walletservice.domain.model.BudgetType;

import java.math.BigDecimal;

public record UpdateBudgetCommand(
        BudgetType budgetType,
        BigDecimal amount
) {}

