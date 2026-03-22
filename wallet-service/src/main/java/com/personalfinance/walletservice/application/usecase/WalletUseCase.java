package com.personalfinance.walletservice.application.usecase;

import com.personalfinance.walletservice.application.dto.CreateWalletCommand;
import com.personalfinance.walletservice.application.dto.UpdateWalletCommand;
import com.personalfinance.walletservice.application.dto.WalletDto;

import java.util.List;
import java.util.UUID;

public interface WalletUseCase {
    WalletDto create(UUID userId, CreateWalletCommand command);
    WalletDto getById(UUID userId, UUID walletId);
    List<WalletDto> getAllByUser(UUID userId);
    WalletDto update(UUID userId, UUID walletId, UpdateWalletCommand command);
    void delete(UUID userId, UUID walletId);
}
