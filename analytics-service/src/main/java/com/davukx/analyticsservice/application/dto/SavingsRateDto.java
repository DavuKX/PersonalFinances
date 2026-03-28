package com.davukx.analyticsservice.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SavingsRateDto(
        UUID userId,
        UUID walletId,
        int year,
        int month,
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal netSavings,
        BigDecimal savingsRate
) {}

