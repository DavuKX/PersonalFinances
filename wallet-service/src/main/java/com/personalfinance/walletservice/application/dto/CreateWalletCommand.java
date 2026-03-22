package com.personalfinance.walletservice.application.dto;

import java.math.BigDecimal;

public record CreateWalletCommand(String name, String currency, BigDecimal balance) {}
