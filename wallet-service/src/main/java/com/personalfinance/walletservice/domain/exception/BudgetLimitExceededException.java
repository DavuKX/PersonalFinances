package com.personalfinance.walletservice.domain.exception;

public class BudgetLimitExceededException extends RuntimeException {
    public BudgetLimitExceededException(String message) {
        super(message);
    }
}

