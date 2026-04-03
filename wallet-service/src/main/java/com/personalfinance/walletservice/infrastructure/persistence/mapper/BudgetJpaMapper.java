package com.personalfinance.walletservice.infrastructure.persistence.mapper;

import com.personalfinance.walletservice.domain.model.Budget;
import com.personalfinance.walletservice.infrastructure.persistence.entity.BudgetJpaEntity;

public class BudgetJpaMapper {

    private BudgetJpaMapper() {}

    public static BudgetJpaEntity toEntity(Budget budget) {
        BudgetJpaEntity entity = new BudgetJpaEntity();
        entity.setId(budget.getId());
        entity.setWalletId(budget.getWalletId());
        entity.setUserId(budget.getUserId());
        entity.setCategoryId(budget.getCategoryId());
        entity.setBudgetType(budget.getBudgetType());
        entity.setAmount(budget.getAmount());
        entity.setPeriod(budget.getPeriod());
        entity.setCreatedAt(budget.getCreatedAt());
        entity.setUpdatedAt(budget.getUpdatedAt());
        return entity;
    }

    public static Budget toDomain(BudgetJpaEntity entity) {
        return new Budget(
                entity.getId(),
                entity.getWalletId(),
                entity.getUserId(),
                entity.getCategoryId(),
                entity.getBudgetType(),
                entity.getAmount(),
                entity.getPeriod(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

