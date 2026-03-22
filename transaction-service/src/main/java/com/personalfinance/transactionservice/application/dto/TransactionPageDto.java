package com.personalfinance.transactionservice.application.dto;

import java.util.List;

public record TransactionPageDto(
        List<TransactionDto> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
