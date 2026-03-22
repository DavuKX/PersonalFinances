package com.personalfinance.walletservice.application.dto;

import java.util.List;

public record WalletPageDto(List<WalletDto> content, int page, int size, long totalElements, int totalPages) {}
