package com.personalfinance.walletservice.application.dto;

import com.personalfinance.walletservice.domain.model.BudgetType;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateBudgetCommand(
        UUID categoryId,
        BudgetType budgetType,
        BigDecimal amount
) {}

