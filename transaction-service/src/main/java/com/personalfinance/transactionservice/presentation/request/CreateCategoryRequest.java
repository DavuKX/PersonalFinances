package com.personalfinance.transactionservice.presentation.request;

import com.personalfinance.transactionservice.domain.model.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class CreateCategoryRequest {

    @NotBlank
    private String name;

    private UUID parentId;

    @NotNull
    private TransactionType transactionType;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public UUID getParentId() { return parentId; }
    public void setParentId(UUID parentId) { this.parentId = parentId; }

    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }
}

