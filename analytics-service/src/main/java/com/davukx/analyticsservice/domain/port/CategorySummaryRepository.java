package com.davukx.analyticsservice.domain.port;

import com.davukx.analyticsservice.domain.model.CategorySummary;
import com.davukx.analyticsservice.domain.model.TransactionType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategorySummaryRepository {
    CategorySummary save(CategorySummary summary);
    Optional<CategorySummary> findUnique(UUID userId, UUID walletId, UUID categoryId, int year, int month, TransactionType type);
    List<CategorySummary> findByUserIdAndWalletIdAndYearAndMonth(UUID userId, UUID walletId, int year, int month);
    List<CategorySummary> findByUserIdAndYearAndMonth(UUID userId, int year, int month);
    List<CategorySummary> findByUserIdAndWalletIdAndTypeAndPeriod(UUID userId, UUID walletId, TransactionType type, int fromYear, int fromMonth, int toYear, int toMonth);
    List<CategorySummary> findByUserIdAndTypeAndPeriod(UUID userId, TransactionType type, int fromYear, int fromMonth, int toYear, int toMonth);
}

