package com.personalfinance.walletservice.application.service;

import com.personalfinance.walletservice.application.dto.CreateWalletCommand;
import com.personalfinance.walletservice.application.dto.SetSpendingLimitCommand;
import com.personalfinance.walletservice.application.dto.UpdateWalletCommand;
import com.personalfinance.walletservice.application.dto.WalletDto;
import com.personalfinance.walletservice.application.dto.WalletPageDto;
import com.personalfinance.walletservice.application.dto.WalletTotalsDto;
import com.personalfinance.walletservice.application.usecase.WalletUseCase;
import com.personalfinance.walletservice.domain.exception.WalletArchivedStateException;
import com.personalfinance.walletservice.domain.exception.WalletNotFoundException;
import com.personalfinance.walletservice.domain.model.SpendingLimit;
import com.personalfinance.walletservice.domain.model.Wallet;
import com.personalfinance.walletservice.domain.port.WalletRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public WalletPageDto listByUser(UUID userId, boolean includeArchived, Pageable pageable) {
        Page<Wallet> page = walletRepository.findByUserIdAndArchived(userId, includeArchived, pageable);
        return new WalletPageDto(
                page.getContent().stream().map(this::toDto).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Override
    @Transactional
    public WalletDto update(UUID userId, UUID walletId, UpdateWalletCommand command) {
        Wallet wallet = loadActiveWallet(userId, walletId);
        return toDto(walletRepository.save(wallet.withName(command.name())));
    }

    @Override
    @Transactional
    public void delete(UUID userId, UUID walletId) {
        Wallet wallet = loadWallet(userId, walletId);
        walletRepository.delete(wallet);
    }

    @Override
    @Transactional
    public WalletDto setSpendingLimit(UUID userId, UUID walletId, SetSpendingLimitCommand command) {
        Wallet wallet = loadActiveWallet(userId, walletId);
        return toDto(walletRepository.save(wallet.withSpendingLimit(new SpendingLimit(command.amount(), command.period()))));
    }

    @Override
    @Transactional
    public WalletDto removeSpendingLimit(UUID userId, UUID walletId) {
        Wallet wallet = loadActiveWallet(userId, walletId);
        return toDto(walletRepository.save(wallet.withoutSpendingLimit()));
    }

    @Override
    @Transactional
    public WalletDto archive(UUID userId, UUID walletId) {
        Wallet wallet = loadWallet(userId, walletId);
        if (wallet.isArchived()) {
            throw new WalletArchivedStateException("Wallet is already archived");
        }
        return toDto(walletRepository.save(wallet.archive()));
    }

    @Override
    @Transactional
    public WalletDto restore(UUID userId, UUID walletId) {
        Wallet wallet = loadWallet(userId, walletId);
        if (!wallet.isArchived()) {
            throw new WalletArchivedStateException("Wallet is not archived");
        }
        return toDto(walletRepository.save(wallet.restore()));
    }

    @Override
    public WalletTotalsDto getTotals(UUID userId) {
        Map<String, java.math.BigDecimal> totals = walletRepository.findByUserId(userId).stream()
                .filter(w -> !w.isArchived())
                .collect(Collectors.groupingBy(
                        Wallet::getCurrency,
                        Collectors.reducing(java.math.BigDecimal.ZERO, Wallet::getBalance, java.math.BigDecimal::add)
                ));
        List<WalletTotalsDto.CurrencyTotal> list = totals.entrySet().stream()
                .map(e -> new WalletTotalsDto.CurrencyTotal(e.getKey(), e.getValue()))
                .toList();
        return new WalletTotalsDto(list);
    }

    private Wallet loadWallet(UUID userId, UUID walletId) {
        return walletRepository.findByIdAndUserId(walletId, userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
    }

    private Wallet loadActiveWallet(UUID userId, UUID walletId) {
        Wallet wallet = loadWallet(userId, walletId);
        if (wallet.isArchived()) {
            throw new WalletArchivedStateException("Cannot modify an archived wallet");
        }
        return wallet;
    }

    private WalletDto toDto(Wallet w) {
        return new WalletDto(
                w.getId(), w.getUserId(), w.getName(), w.getCurrency(), w.getBalance(),
                w.getSpendingLimit() != null ? w.getSpendingLimit().getAmount() : null,
                w.getSpendingLimit() != null ? w.getSpendingLimit().getPeriod() : null,
                w.isArchived(), w.getArchivedAt(),
                w.getCreatedAt(), w.getUpdatedAt()
        );
    }
}
