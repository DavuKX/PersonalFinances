package com.personalfinance.walletservice.domain.port;

import com.personalfinance.walletservice.domain.model.Wallet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository {
    Wallet save(Wallet wallet);
    Optional<Wallet> findByIdAndUserId(UUID id, UUID userId);
    Optional<Wallet> findById(UUID id);
    List<Wallet> findByUserId(UUID userId);
    Page<Wallet> findByUserIdAndArchived(UUID userId, boolean archived, Pageable pageable);
    void delete(Wallet wallet);
}
