package com.personalfinance.walletservice.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class Wallet {

    private final UUID id;
    private final UUID userId;
    private final String name;
    private final String currency;
    private final BigDecimal balance;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    public Wallet(UUID id, UUID userId, String name, String currency, BigDecimal balance,
                  OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.currency = currency;
        this.balance = balance;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Wallet create(UUID userId, String name, String currency, BigDecimal balance) {
        OffsetDateTime now = OffsetDateTime.now();
        return new Wallet(UUID.randomUUID(), userId, name, currency.toUpperCase(),
                balance != null ? balance : BigDecimal.ZERO, now, now);
    }

    public Wallet withName(String name) {
        return new Wallet(id, userId, name, currency, balance, createdAt, OffsetDateTime.now());
    }

    public Wallet withBalance(BigDecimal balance) {
        return new Wallet(id, userId, name, currency, balance, createdAt, OffsetDateTime.now());
    }

    public boolean belongsTo(UUID ownerId) {
        return userId.equals(ownerId);
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getName() { return name; }
    public String getCurrency() { return currency; }
    public BigDecimal getBalance() { return balance; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
