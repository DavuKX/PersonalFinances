package com.personalfinance.transactionservice.application.dto;

import com.personalfinance.transactionservice.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record UpdateTransactionCommand(
        TransactionType type,
        BigDecimal amount,
        String category,
        String subCategory,
        String description,
        OffsetDateTime transactionDate
) {}
