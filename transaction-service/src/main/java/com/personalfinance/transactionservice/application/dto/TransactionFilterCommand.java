package com.personalfinance.transactionservice.application.dto;

import com.personalfinance.transactionservice.domain.model.TransactionType;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TransactionFilterCommand(
        UUID userId,
        UUID walletId,
        TransactionType type,
        String category,
        OffsetDateTime from,
        OffsetDateTime to,
        Pageable pageable
) {}
