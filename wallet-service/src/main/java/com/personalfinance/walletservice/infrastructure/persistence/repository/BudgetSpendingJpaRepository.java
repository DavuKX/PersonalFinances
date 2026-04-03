package com.personalfinance.walletservice.infrastructure.persistence.repository;

import com.personalfinance.walletservice.infrastructure.persistence.entity.BudgetSpendingJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetSpendingJpaRepository extends JpaRepository<BudgetSpendingJpaEntity, UUID> {
    Optional<BudgetSpendingJpaEntity> findByWalletIdAndCategoryIdAndPeriodStart(UUID walletId, UUID categoryId, LocalDate periodStart);
    List<BudgetSpendingJpaEntity> findByWalletIdAndPeriodStart(UUID walletId, LocalDate periodStart);
}

