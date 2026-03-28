package com.personalfinance.transactionservice.application.dto;

import com.personalfinance.transactionservice.domain.model.TransactionType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CategoryDto(
        UUID id,
        UUID userId,
        String name,
        UUID parentId,
        TransactionType transactionType,
        boolean isDefault,
        OffsetDateTime createdAt
) {}

