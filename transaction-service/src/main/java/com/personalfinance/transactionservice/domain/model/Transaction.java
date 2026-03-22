package com.personalfinance.transactionservice.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class Transaction {

    private final UUID id;
    private final UUID userId;
    private final UUID walletId;
    private final TransactionType type;
    private final BigDecimal amount;
    private final String currency;
    private final String category;
    private final String subCategory;
    private final String description;
    private final OffsetDateTime transactionDate;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    public Transaction(UUID id, UUID userId, UUID walletId, TransactionType type, BigDecimal amount,
                       String currency, String category, String subCategory, String description,
                       OffsetDateTime transactionDate, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.walletId = walletId;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.category = category;
        this.subCategory = subCategory;
        this.description = description;
        this.transactionDate = transactionDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Transaction create(UUID userId, UUID walletId, TransactionType type, BigDecimal amount,
                                     String currency, String category, String subCategory,
                                     String description, OffsetDateTime transactionDate) {
        OffsetDateTime now = OffsetDateTime.now();
        return new Transaction(UUID.randomUUID(), userId, walletId, type, amount, currency.toUpperCase(),
                category, subCategory, description,
                transactionDate != null ? transactionDate : now, now, now);
    }

    public Transaction withDetails(TransactionType type, BigDecimal amount, String category,
                                   String subCategory, String description, OffsetDateTime transactionDate) {
        return new Transaction(id, userId, walletId, type, amount, currency, category, subCategory,
                description, transactionDate, createdAt, OffsetDateTime.now());
    }

    public boolean belongsTo(UUID ownerId) {
        return userId.equals(ownerId);
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getWalletId() { return walletId; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getCategory() { return category; }
    public String getSubCategory() { return subCategory; }
    public String getDescription() { return description; }
    public OffsetDateTime getTransactionDate() { return transactionDate; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
