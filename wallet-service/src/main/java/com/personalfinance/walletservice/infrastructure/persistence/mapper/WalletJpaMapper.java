package com.personalfinance.walletservice.infrastructure.persistence.mapper;

import com.personalfinance.walletservice.domain.model.SpendingLimit;
import com.personalfinance.walletservice.domain.model.Wallet;
import com.personalfinance.walletservice.infrastructure.persistence.entity.WalletJpaEntity;

public class WalletJpaMapper {

    private WalletJpaMapper() {}

    public static WalletJpaEntity toEntity(Wallet wallet) {
        WalletJpaEntity entity = new WalletJpaEntity();
        entity.setId(wallet.getId());
        entity.setUserId(wallet.getUserId());
        entity.setName(wallet.getName());
        entity.setCurrency(wallet.getCurrency());
        entity.setBalance(wallet.getBalance());
        entity.setSpendingLimitAmount(wallet.getSpendingLimit() != null ? wallet.getSpendingLimit().getAmount() : null);
        entity.setSpendingLimitPeriod(wallet.getSpendingLimit() != null ? wallet.getSpendingLimit().getPeriod() : null);
        entity.setArchived(wallet.isArchived());
        entity.setArchivedAt(wallet.getArchivedAt());
        entity.setCreatedAt(wallet.getCreatedAt());
        entity.setUpdatedAt(wallet.getUpdatedAt());
        return entity;
    }

    public static Wallet toDomain(WalletJpaEntity entity) {
        SpendingLimit spendingLimit = null;
        if (entity.getSpendingLimitAmount() != null && entity.getSpendingLimitPeriod() != null) {
            spendingLimit = new SpendingLimit(entity.getSpendingLimitAmount(), entity.getSpendingLimitPeriod());
        }
        return new Wallet(
                entity.getId(),
                entity.getUserId(),
                entity.getName(),
                entity.getCurrency(),
                entity.getBalance(),
                spendingLimit,
                entity.isArchived(),
                entity.getArchivedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
