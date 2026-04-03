package com.personalfinance.walletservice.domain.port;

import com.personalfinance.walletservice.domain.model.Budget;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository {
    Budget save(Budget budget);
    Optional<Budget> findById(UUID id);
    List<Budget> findByWalletIdAndUserId(UUID walletId, UUID userId);
    Optional<Budget> findByWalletIdAndCategoryId(UUID walletId, UUID categoryId);
    void deleteById(UUID id);
    void deleteAllByWalletId(UUID walletId);
}

