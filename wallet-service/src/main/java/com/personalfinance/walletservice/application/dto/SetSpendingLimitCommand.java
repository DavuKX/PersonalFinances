package com.personalfinance.walletservice.application.dto;

import com.personalfinance.walletservice.domain.model.LimitPeriod;

import java.math.BigDecimal;

public record SetSpendingLimitCommand(BigDecimal amount, LimitPeriod period) {}
