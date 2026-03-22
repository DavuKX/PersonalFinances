package com.personalfinance.walletservice.domain.exception;

public class WalletArchivedStateException extends RuntimeException {
    public WalletArchivedStateException(String message) {
        super(message);
    }
}
