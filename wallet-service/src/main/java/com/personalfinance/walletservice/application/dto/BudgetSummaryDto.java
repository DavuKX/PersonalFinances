package com.personalfinance.walletservice.application.dto;

import com.personalfinance.walletservice.domain.model.BudgetPeriod;
import com.personalfinance.walletservice.domain.model.BudgetType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record BudgetSummaryDto(
        UUID id,
        UUID walletId,
        UUID userId,
        UUID categoryId,
        BudgetType budgetType,
        BigDecimal amount,
        BigDecimal resolvedAmount,
        BudgetPeriod period,
        BigDecimal spentAmount,
        BigDecimal remainingAmount,
        double percentUsed,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}

