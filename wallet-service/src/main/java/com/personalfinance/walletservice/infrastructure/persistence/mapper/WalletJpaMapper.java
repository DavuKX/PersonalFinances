package com.personalfinance.walletservice.infrastructure.persistence.mapper;

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
        entity.setCreatedAt(wallet.getCreatedAt());
        entity.setUpdatedAt(wallet.getUpdatedAt());
        return entity;
    }

    public static Wallet toDomain(WalletJpaEntity entity) {
        return new Wallet(
                entity.getId(),
                entity.getUserId(),
                entity.getName(),
                entity.getCurrency(),
                entity.getBalance(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
