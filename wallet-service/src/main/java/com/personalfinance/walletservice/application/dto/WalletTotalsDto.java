package com.personalfinance.walletservice.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record WalletTotalsDto(List<CurrencyTotal> totals) {
    public record CurrencyTotal(String currency, BigDecimal total) {}
}
