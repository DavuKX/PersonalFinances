package com.personalfinance.walletservice.application.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record WalletDto(
        UUID id,
        UUID userId,
        String name,
        String currency,
        BigDecimal balance,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
