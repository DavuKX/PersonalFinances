package com.personalfinance.walletservice.domain.model;

import java.math.BigDecimal;

public class SpendingLimit {

    private final BigDecimal amount;
    private final LimitPeriod period;

    public SpendingLimit(BigDecimal amount, LimitPeriod period) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Spending limit amount must be positive");
        }
        if (period == null) {
            throw new IllegalArgumentException("Spending limit period is required");
        }
        this.amount = amount;
        this.period = period;
    }

    public BigDecimal getAmount() { return amount; }
    public LimitPeriod getPeriod() { return period; }
}
