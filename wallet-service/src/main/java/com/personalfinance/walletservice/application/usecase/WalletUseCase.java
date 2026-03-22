package com.personalfinance.walletservice.application.usecase;

import com.personalfinance.walletservice.application.dto.CreateWalletCommand;
import com.personalfinance.walletservice.application.dto.SetSpendingLimitCommand;
import com.personalfinance.walletservice.application.dto.UpdateWalletCommand;
import com.personalfinance.walletservice.application.dto.WalletDto;
import com.personalfinance.walletservice.application.dto.WalletPageDto;
import com.personalfinance.walletservice.application.dto.WalletTotalsDto;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface WalletUseCase {
    WalletDto create(UUID userId, CreateWalletCommand command);
    WalletDto getById(UUID userId, UUID walletId);
    List<WalletDto> getAllByUser(UUID userId);
    WalletPageDto listByUser(UUID userId, boolean includeArchived, Pageable pageable);
    WalletDto update(UUID userId, UUID walletId, UpdateWalletCommand command);
    void delete(UUID userId, UUID walletId);
    WalletDto setSpendingLimit(UUID userId, UUID walletId, SetSpendingLimitCommand command);
    WalletDto removeSpendingLimit(UUID userId, UUID walletId);
    WalletDto archive(UUID userId, UUID walletId);
    WalletDto restore(UUID userId, UUID walletId);
    WalletTotalsDto getTotals(UUID userId);
}
