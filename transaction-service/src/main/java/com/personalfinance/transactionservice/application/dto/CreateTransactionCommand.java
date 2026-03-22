package com.personalfinance.transactionservice.application.dto;

import com.personalfinance.transactionservice.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateTransactionCommand(
        UUID walletId,
        TransactionType type,
        BigDecimal amount,
        String currency,
        String category,
        String subCategory,
        String description,
        OffsetDateTime transactionDate
) {}
