package com.davukx.analyticsservice.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record MonthlyAnalyticsDto(
        UUID userId,
        UUID walletId,
        int year,
        int month,
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal totalSavings,
        BigDecimal netSavings,
        BigDecimal savingsRate,
        int transactionCount
) {}

