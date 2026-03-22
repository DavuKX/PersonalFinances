package com.personalfinance.walletservice.infrastructure.persistence.adapter;

import com.personalfinance.walletservice.domain.model.Wallet;
import com.personalfinance.walletservice.domain.port.WalletRepository;
import com.personalfinance.walletservice.infrastructure.persistence.mapper.WalletJpaMapper;
import com.personalfinance.walletservice.infrastructure.persistence.repository.WalletJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class WalletRepositoryAdapter implements WalletRepository {

    private final WalletJpaRepository jpaRepository;

    public WalletRepositoryAdapter(WalletJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Wallet save(Wallet wallet) {
        return WalletJpaMapper.toDomain(jpaRepository.save(WalletJpaMapper.toEntity(wallet)));
    }

    @Override
    public Optional<Wallet> findByIdAndUserId(UUID id, UUID userId) {
        return jpaRepository.findByIdAndUserId(id, userId).map(WalletJpaMapper::toDomain);
    }

    @Override
    public Optional<Wallet> findById(UUID id) {
        return jpaRepository.findById(id).map(WalletJpaMapper::toDomain);
    }

    @Override
    public List<Wallet> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).stream().map(WalletJpaMapper::toDomain).toList();
    }

    @Override
    public void delete(Wallet wallet) {
        jpaRepository.deleteById(wallet.getId());
    }
}
