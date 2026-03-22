package com.personalfinance.walletservice.presentation.response;

import com.personalfinance.walletservice.application.dto.WalletTotalsDto;

import java.util.List;

public class WalletTotalsResponse {

    private List<WalletTotalsDto.CurrencyTotal> totals;

    public WalletTotalsResponse() {}

    public WalletTotalsResponse(List<WalletTotalsDto.CurrencyTotal> totals) {
        this.totals = totals;
    }

    public List<WalletTotalsDto.CurrencyTotal> getTotals() { return totals; }
    public void setTotals(List<WalletTotalsDto.CurrencyTotal> totals) { this.totals = totals; }
}
