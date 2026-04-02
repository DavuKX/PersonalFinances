package com.davukx.analyticsservice.application.dto;

import java.math.BigDecimal;

public record TrendPointDto(
        int year,
        int month,
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal totalSavings,
        BigDecimal netSavings,
        BigDecimal savingsRate,
        int transactionCount
) {}

