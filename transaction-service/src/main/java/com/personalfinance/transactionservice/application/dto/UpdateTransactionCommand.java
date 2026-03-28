package com.personalfinance.transactionservice.application.dto;

import com.personalfinance.transactionservice.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record UpdateTransactionCommand(
        TransactionType type,
        BigDecimal amount,
        UUID categoryId,
        UUID subCategoryId,
        String description,
        OffsetDateTime transactionDate
) {}
