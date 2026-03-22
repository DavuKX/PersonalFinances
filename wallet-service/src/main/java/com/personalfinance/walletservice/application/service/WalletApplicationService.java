package com.personalfinance.walletservice.application.service;

import com.personalfinance.walletservice.application.dto.CreateWalletCommand;
import com.personalfinance.walletservice.application.dto.UpdateWalletCommand;
import com.personalfinance.walletservice.application.dto.WalletDto;
import com.personalfinance.walletservice.application.usecase.WalletUseCase;
import com.personalfinance.walletservice.domain.exception.WalletNotFoundException;
import com.personalfinance.walletservice.domain.model.Wallet;
import com.personalfinance.walletservice.domain.port.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class WalletApplicationService implements WalletUseCase {

    private final WalletRepository walletRepository;

    public WalletApplicationService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Override
    @Transactional
    public WalletDto create(UUID userId, CreateWalletCommand command) {
        Wallet wallet = Wallet.create(userId, command.name(), command.currency(), command.balance());
        return toDto(walletRepository.save(wallet));
    }

    @Override
    public WalletDto getById(UUID userId, UUID walletId) {
        return toDto(loadWallet(userId, walletId));
    }

    @Override
    public List<WalletDto> getAllByUser(UUID userId) {
        return walletRepository.findByUserId(userId).stream().map(this::toDto).toList();
    }

    @Override
    @Transactional
    public WalletDto update(UUID userId, UUID walletId, UpdateWalletCommand command) {
        Wallet wallet = loadWallet(userId, walletId);
        return toDto(walletRepository.save(wallet.withName(command.name())));
    }

    @Override
    @Transactional
    public void delete(UUID userId, UUID walletId) {
        Wallet wallet = loadWallet(userId, walletId);
        walletRepository.delete(wallet);
    }

    private Wallet loadWallet(UUID userId, UUID walletId) {
        return walletRepository.findByIdAndUserId(walletId, userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
    }

    private WalletDto toDto(Wallet w) {
        return new WalletDto(w.getId(), w.getUserId(), w.getName(), w.getCurrency(),
                w.getBalance(), w.getCreatedAt(), w.getUpdatedAt());
    }
}
