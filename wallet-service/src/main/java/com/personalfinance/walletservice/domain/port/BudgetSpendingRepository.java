package com.personalfinance.walletservice.domain.port;

import com.personalfinance.walletservice.domain.model.BudgetSpending;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetSpendingRepository {
    BudgetSpending save(BudgetSpending spending);
    Optional<BudgetSpending> findByWalletIdAndCategoryIdAndPeriodStart(UUID walletId, UUID categoryId, LocalDate periodStart);
    List<BudgetSpending> findByWalletIdAndPeriodStart(UUID walletId, LocalDate periodStart);
}

