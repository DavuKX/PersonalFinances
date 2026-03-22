package com.personalfinance.walletservice.domain.port;

import com.personalfinance.walletservice.domain.model.Wallet;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository {
    Wallet save(Wallet wallet);
    Optional<Wallet> findByIdAndUserId(UUID id, UUID userId);
    Optional<Wallet> findById(UUID id);
    List<Wallet> findByUserId(UUID userId);
    void delete(Wallet wallet);
}
