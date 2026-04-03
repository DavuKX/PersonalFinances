package com.personalfinance.walletservice.infrastructure.persistence.repository;

import com.personalfinance.walletservice.infrastructure.persistence.entity.BudgetJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetJpaRepository extends JpaRepository<BudgetJpaEntity, UUID> {
    List<BudgetJpaEntity> findByWalletIdAndUserId(UUID walletId, UUID userId);
    Optional<BudgetJpaEntity> findByWalletIdAndCategoryId(UUID walletId, UUID categoryId);
    void deleteAllByWalletId(UUID walletId);
}

