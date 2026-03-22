package com.personalfinance.walletservice.infrastructure.messaging.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.UUID;

public class TransactionDeletedEvent {

    @JsonProperty
    private UUID transactionId;

    @JsonProperty
    private UUID userId;

    @JsonProperty
    private UUID walletId;

    @JsonProperty
    private String type;

    @JsonProperty
    private BigDecimal amount;

    @JsonProperty
    private String currency;

    public TransactionDeletedEvent() {}

    public TransactionDeletedEvent(UUID transactionId, UUID userId, UUID walletId,
                                   String type, BigDecimal amount, String currency) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.walletId = walletId;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
    }

    public UUID getTransactionId() { return transactionId; }
    public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getWalletId() { return walletId; }
    public void setWalletId(UUID walletId) { this.walletId = walletId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
