package com.personalfinance.transactionservice.infrastructure.messaging.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.personalfinance.transactionservice.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class TransactionDeletedEvent {

    @JsonProperty
    private UUID transactionId;

    @JsonProperty
    private UUID userId;

    @JsonProperty
    private UUID walletId;

    @JsonProperty
    private TransactionType type;

    @JsonProperty
    private BigDecimal amount;

    @JsonProperty
    private String currency;

    @JsonProperty
    private UUID categoryId;

    @JsonProperty
    private UUID subCategoryId;

    @JsonProperty
    private OffsetDateTime transactionDate;

    public TransactionDeletedEvent() {}

    public TransactionDeletedEvent(UUID transactionId, UUID userId, UUID walletId,
                                   TransactionType type, BigDecimal amount, String currency,
                                   UUID categoryId, UUID subCategoryId, OffsetDateTime transactionDate) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.walletId = walletId;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.categoryId = categoryId;
        this.subCategoryId = subCategoryId;
        this.transactionDate = transactionDate;
    }

    public UUID getTransactionId() { return transactionId; }
    public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getWalletId() { return walletId; }
    public void setWalletId(UUID walletId) { this.walletId = walletId; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }

    public UUID getSubCategoryId() { return subCategoryId; }
    public void setSubCategoryId(UUID subCategoryId) { this.subCategoryId = subCategoryId; }

    public OffsetDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(OffsetDateTime transactionDate) { this.transactionDate = transactionDate; }
}
