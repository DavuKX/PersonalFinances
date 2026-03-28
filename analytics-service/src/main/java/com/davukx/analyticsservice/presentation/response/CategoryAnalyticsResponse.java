package com.davukx.analyticsservice.presentation.response;

import com.davukx.analyticsservice.domain.model.TransactionType;

import java.math.BigDecimal;
import java.util.UUID;

public class CategoryAnalyticsResponse {
    private UUID categoryId;
    private TransactionType transactionType;
    private int year;
    private int month;
    private BigDecimal totalAmount;
    private int transactionCount;

    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }
    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public int getTransactionCount() { return transactionCount; }
    public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }
}

