package com.personalfinance.walletservice.service;

import com.personalfinance.walletservice.dto.CreateWalletRequest;
import com.personalfinance.walletservice.dto.UpdateWalletRequest;
import com.personalfinance.walletservice.dto.WalletResponse;
import com.personalfinance.walletservice.entity.Wallet;
import com.personalfinance.walletservice.exception.ResourceNotFoundException;
import com.personalfinance.walletservice.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Transactional
    public WalletResponse createWallet(UUID userId, CreateWalletRequest request) {
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setName(request.getName());
        wallet.setCurrency(request.getCurrency().toUpperCase());
        wallet.setBalance(request.getBalance() != null ? request.getBalance() : BigDecimal.ZERO);
        Wallet saved = walletRepository.save(wallet);
        return toResponse(saved);
    }

    public WalletResponse getWalletById(UUID userId, UUID walletId) {
        Wallet wallet = walletRepository.findByIdAndUserId(walletId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
        return toResponse(wallet);
    }

    public List<WalletResponse> getWalletsByUser(UUID userId) {
        return walletRepository.findByUserId(userId).stream()
                .map(WalletService::toResponse)
                .toList();
    }

    @Transactional
    public WalletResponse updateWallet(UUID userId, UUID walletId, UpdateWalletRequest request) {
        Wallet wallet = walletRepository.findByIdAndUserId(walletId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
        wallet.setName(request.getName());
        Wallet saved = walletRepository.save(wallet);
        return toResponse(saved);
    }

    @Transactional
    public void deleteWallet(UUID userId, UUID walletId) {
        Wallet wallet = walletRepository.findByIdAndUserId(walletId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
        walletRepository.delete(wallet);
    }

    public static WalletResponse toResponse(Wallet w) {
        WalletResponse r = new WalletResponse();
        r.setId(w.getId());
        r.setUserId(w.getUserId());
        r.setName(w.getName());
        r.setCurrency(w.getCurrency());
        r.setBalance(w.getBalance());
        r.setCreatedAt(w.getCreatedAt());
        r.setUpdatedAt(w.getUpdatedAt());
        return r;
    }
}
