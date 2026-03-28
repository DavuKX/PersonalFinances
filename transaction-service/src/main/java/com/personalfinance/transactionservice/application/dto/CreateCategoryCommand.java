package com.personalfinance.transactionservice.application.dto;

import com.personalfinance.transactionservice.domain.model.TransactionType;

import java.util.UUID;

public record CreateCategoryCommand(
        String name,
        UUID parentId,
        TransactionType transactionType
) {}

