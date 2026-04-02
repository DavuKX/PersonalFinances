package com.davukx.analyticsservice.presentation.response;

import java.math.BigDecimal;
import java.util.UUID;

public class SavingsRateResponse {
    private UUID userId;
    private UUID walletId;
    private int year;
    private int month;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal totalSavings;
    private BigDecimal netSavings;
    private BigDecimal savingsRate;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getWalletId() { return walletId; }
    public void setWalletId(UUID walletId) { this.walletId = walletId; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }
    public BigDecimal getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(BigDecimal totalExpenses) { this.totalExpenses = totalExpenses; }
    public BigDecimal getTotalSavings() { return totalSavings; }
    public void setTotalSavings(BigDecimal totalSavings) { this.totalSavings = totalSavings; }
    public BigDecimal getNetSavings() { return netSavings; }
    public void setNetSavings(BigDecimal netSavings) { this.netSavings = netSavings; }
    public BigDecimal getSavingsRate() { return savingsRate; }
    public void setSavingsRate(BigDecimal savingsRate) { this.savingsRate = savingsRate; }
}

