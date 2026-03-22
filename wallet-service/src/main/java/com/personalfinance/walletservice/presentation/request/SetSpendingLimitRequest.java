package com.personalfinance.walletservice.presentation.request;

import com.personalfinance.walletservice.domain.model.LimitPeriod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class SetSpendingLimitRequest {

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    private LimitPeriod period;

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LimitPeriod getPeriod() { return period; }
    public void setPeriod(LimitPeriod period) { this.period = period; }
}
