package com.davukx.analyticsservice.presentation.response;

import java.math.BigDecimal;

public class TrendResponse {
    private int year;
    private int month;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal totalSavings;
    private BigDecimal netSavings;
    private BigDecimal savingsRate;
    private int transactionCount;

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
    public int getTransactionCount() { return transactionCount; }
    public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }
}

