package com.personalfinance.walletservice.application.dto;

import com.personalfinance.walletservice.domain.model.BudgetPeriod;
import com.personalfinance.walletservice.domain.model.BudgetType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record BudgetDto(
        UUID id,
        UUID walletId,
        UUID userId,
        UUID categoryId,
        BudgetType budgetType,
        BigDecimal amount,
        BudgetPeriod period,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}

