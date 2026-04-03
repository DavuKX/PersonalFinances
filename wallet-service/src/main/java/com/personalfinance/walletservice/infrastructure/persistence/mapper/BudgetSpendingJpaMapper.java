package com.personalfinance.walletservice.infrastructure.persistence.mapper;

import com.personalfinance.walletservice.domain.model.BudgetSpending;
import com.personalfinance.walletservice.infrastructure.persistence.entity.BudgetSpendingJpaEntity;

public class BudgetSpendingJpaMapper {

    private BudgetSpendingJpaMapper() {}

    public static BudgetSpendingJpaEntity toEntity(BudgetSpending spending) {
        BudgetSpendingJpaEntity entity = new BudgetSpendingJpaEntity();
        entity.setId(spending.getId());
        entity.setWalletId(spending.getWalletId());
        entity.setUserId(spending.getUserId());
        entity.setCategoryId(spending.getCategoryId());
        entity.setPeriodStart(spending.getPeriodStart());
        entity.setPeriodEnd(spending.getPeriodEnd());
        entity.setTotalSpent(spending.getTotalSpent());
        entity.setUpdatedAt(spending.getUpdatedAt());
        return entity;
    }

    public static BudgetSpending toDomain(BudgetSpendingJpaEntity entity) {
        return new BudgetSpending(
                entity.getId(),
                entity.getWalletId(),
                entity.getUserId(),
                entity.getCategoryId(),
                entity.getPeriodStart(),
                entity.getPeriodEnd(),
                entity.getTotalSpent(),
                entity.getUpdatedAt()
        );
    }
}

