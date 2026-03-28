package com.davukx.analyticsservice.domain.port;

import com.davukx.analyticsservice.domain.model.MonthlySummary;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MonthlySummaryRepository {
    MonthlySummary save(MonthlySummary summary);
    Optional<MonthlySummary> findByUserIdAndWalletIdAndYearAndMonth(UUID userId, UUID walletId, int year, int month);
    List<MonthlySummary> findByUserIdAndYearAndMonth(UUID userId, int year, int month);
    List<MonthlySummary> findByUserIdAndWalletIdAndPeriod(UUID userId, UUID walletId, int fromYear, int fromMonth, int toYear, int toMonth);
    List<MonthlySummary> findByUserIdAndPeriod(UUID userId, int fromYear, int fromMonth, int toYear, int toMonth);
    List<MonthlySummary> findByUserIdAndYear(UUID userId, int year);
    List<MonthlySummary> findByUserIdAndWalletIdAndYear(UUID userId, UUID walletId, int year);
}

