package com.personalfinance.walletservice.infrastructure.persistence.repository;

import com.personalfinance.walletservice.infrastructure.persistence.entity.WalletJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletJpaRepository extends JpaRepository<WalletJpaEntity, UUID> {
    List<WalletJpaEntity> findByUserId(UUID userId);
    Optional<WalletJpaEntity> findByIdAndUserId(UUID id, UUID userId);
}
