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
    private final SpendingLimit spendingLimit;
    private final BigDecimal monthlyIncome;
    private final boolean archived;
    private final OffsetDateTime archivedAt;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    public Wallet(UUID id, UUID userId, String name, String currency, BigDecimal balance,
                  SpendingLimit spendingLimit, BigDecimal monthlyIncome, boolean archived,
                  OffsetDateTime archivedAt, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.currency = currency;
        this.balance = balance;
        this.spendingLimit = spendingLimit;
        this.monthlyIncome = monthlyIncome;
        this.archived = archived;
        this.archivedAt = archivedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Wallet create(UUID userId, String name, String currency, BigDecimal balance) {
        OffsetDateTime now = OffsetDateTime.now();
        return new Wallet(UUID.randomUUID(), userId, name, currency.toUpperCase(),
                balance != null ? balance : BigDecimal.ZERO, null, null, false, null, now, now);
    }

    public Wallet withName(String name) {
        return new Wallet(id, userId, name, currency, balance, spendingLimit, monthlyIncome, archived, archivedAt, createdAt, OffsetDateTime.now());
    }

    public Wallet withBalance(BigDecimal balance) {
        return new Wallet(id, userId, name, currency, balance, spendingLimit, monthlyIncome, archived, archivedAt, createdAt, OffsetDateTime.now());
    }

    public Wallet adjustBalance(BigDecimal balanceChange) {
        BigDecimal newBalance = balance.add(balanceChange);
        return new Wallet(id, userId, name, currency, newBalance, spendingLimit, monthlyIncome, archived, archivedAt, createdAt, OffsetDateTime.now());
    }

    public Wallet withSpendingLimit(SpendingLimit spendingLimit) {
        return new Wallet(id, userId, name, currency, balance, spendingLimit, monthlyIncome, archived, archivedAt, createdAt, OffsetDateTime.now());
    }

    public Wallet withoutSpendingLimit() {
        return new Wallet(id, userId, name, currency, balance, null, monthlyIncome, archived, archivedAt, createdAt, OffsetDateTime.now());
    }

    public Wallet withMonthlyIncome(BigDecimal monthlyIncome) {
        return new Wallet(id, userId, name, currency, balance, spendingLimit, monthlyIncome, archived, archivedAt, createdAt, OffsetDateTime.now());
    }

    public Wallet archive() {
        return new Wallet(id, userId, name, currency, balance, spendingLimit, monthlyIncome, true, OffsetDateTime.now(), createdAt, OffsetDateTime.now());
    }

    public Wallet restore() {
        return new Wallet(id, userId, name, currency, balance, spendingLimit, monthlyIncome, false, null, createdAt, OffsetDateTime.now());
    }

    public boolean belongsTo(UUID ownerId) {
        return userId.equals(ownerId);
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getName() { return name; }
    public String getCurrency() { return currency; }
    public BigDecimal getBalance() { return balance; }
    public SpendingLimit getSpendingLimit() { return spendingLimit; }
    public BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public boolean isArchived() { return archived; }
    public OffsetDateTime getArchivedAt() { return archivedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
