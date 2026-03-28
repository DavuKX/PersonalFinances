package com.davukx.analyticsservice.application.usecase;

import com.davukx.analyticsservice.application.dto.*;
import com.davukx.analyticsservice.domain.model.TransactionType;
import com.davukx.analyticsservice.infrastructure.messaging.event.TransactionCreatedEvent;
import com.davukx.analyticsservice.infrastructure.messaging.event.TransactionDeletedEvent;

import java.util.List;
import java.util.UUID;

public interface AnalyticsUseCase {
    void processTransactionCreated(TransactionCreatedEvent event);
    void processTransactionDeleted(TransactionDeletedEvent event);
    MonthlyAnalyticsDto getMonthly(UUID userId, UUID walletId, int year, int month);
    List<CategoryAnalyticsDto> getByCategory(UUID userId, UUID walletId, int year, int month, TransactionType type);
    SavingsRateDto getSavingsRate(UUID userId, UUID walletId, int year, int month);
    List<TrendPointDto> getTrend(UUID userId, UUID walletId, int months);
    List<WalletBreakdownDto> getWalletBreakdown(UUID userId, int year, int month);
}

