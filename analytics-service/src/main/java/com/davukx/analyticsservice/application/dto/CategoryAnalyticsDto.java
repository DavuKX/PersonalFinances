package com.davukx.analyticsservice.application.dto;

import com.davukx.analyticsservice.domain.model.TransactionType;

import java.math.BigDecimal;
import java.util.UUID;

public record CategoryAnalyticsDto(
        UUID categoryId,
        TransactionType transactionType,
        int year,
        int month,
        BigDecimal totalAmount,
        int transactionCount
) {}

