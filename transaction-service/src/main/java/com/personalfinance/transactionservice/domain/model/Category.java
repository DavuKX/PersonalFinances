package com.personalfinance.transactionservice.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class Category {

    private final UUID id;
    private final UUID userId;
    private final String name;
    private final UUID parentId;
    private final TransactionType transactionType;
    private final OffsetDateTime createdAt;

    public Category(UUID id, UUID userId, String name, UUID parentId,
                    TransactionType transactionType, OffsetDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.parentId = parentId;
        this.transactionType = transactionType;
        this.createdAt = createdAt;
    }

    public static Category createDefault(String name, UUID parentId, TransactionType transactionType) {
        return new Category(UUID.randomUUID(), null, name, parentId, transactionType, OffsetDateTime.now());
    }

    public static Category createCustom(UUID userId, String name, UUID parentId, TransactionType transactionType) {
        return new Category(UUID.randomUUID(), userId, name, parentId, transactionType, OffsetDateTime.now());
    }

    public boolean isDefault() {
        return userId == null;
    }

    public boolean isTopLevel() {
        return parentId == null;
    }

    public boolean isAccessibleBy(UUID requestUserId) {
        return userId == null || userId.equals(requestUserId);
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getName() { return name; }
    public UUID getParentId() { return parentId; }
    public TransactionType getTransactionType() { return transactionType; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}

