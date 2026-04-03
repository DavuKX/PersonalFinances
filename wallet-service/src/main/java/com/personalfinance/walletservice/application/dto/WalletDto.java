package com.personalfinance.walletservice.application.dto;

import com.personalfinance.walletservice.domain.model.LimitPeriod;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record WalletDto(
        UUID id,
        UUID userId,
        String name,
        String currency,
        BigDecimal balance,
        BigDecimal spendingLimitAmount,
        LimitPeriod spendingLimitPeriod,
        BigDecimal monthlyIncome,
        boolean archived,
        OffsetDateTime archivedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
