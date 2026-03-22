package com.personalfinance.walletservice.service;

import com.personalfinance.walletservice.dto.CreateWalletRequest;
import com.personalfinance.walletservice.dto.UpdateWalletRequest;
import com.personalfinance.walletservice.dto.WalletResponse;
import com.personalfinance.walletservice.entity.Wallet;
import com.personalfinance.walletservice.exception.ResourceNotFoundException;
import com.personalfinance.walletservice.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletService walletService;

    private UUID userId;
    private UUID walletId;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        walletId = UUID.randomUUID();
        wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setUserId(userId);
        wallet.setName("Main Wallet");
        wallet.setCurrency("USD");
        wallet.setBalance(BigDecimal.valueOf(1000));
    }

    @Test
    void createWallet_shouldReturnWalletResponse() {
        CreateWalletRequest request = new CreateWalletRequest();
        request.setName("Main Wallet");
        request.setCurrency("usd");
        request.setBalance(BigDecimal.valueOf(1000));

        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        WalletResponse response = walletService.createWallet(userId, request);

        assertEquals("Main Wallet", response.getName());
        assertEquals("USD", response.getCurrency());
        assertEquals(BigDecimal.valueOf(1000), response.getBalance());
        assertEquals(userId, response.getUserId());
    }

    @Test
    void createWallet_shouldDefaultBalanceToZero() {
        CreateWalletRequest request = new CreateWalletRequest();
        request.setName("Empty Wallet");
        request.setCurrency("eur");

        Wallet savedWallet = new Wallet();
        savedWallet.setId(UUID.randomUUID());
        savedWallet.setUserId(userId);
        savedWallet.setName("Empty Wallet");
        savedWallet.setCurrency("EUR");
        savedWallet.setBalance(BigDecimal.ZERO);

        when(walletRepository.save(any(Wallet.class))).thenReturn(savedWallet);

        WalletResponse response = walletService.createWallet(userId, request);

        assertEquals(BigDecimal.ZERO, response.getBalance());
    }

    @Test
    void getWalletById_shouldReturnWallet() {
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.of(wallet));

        WalletResponse response = walletService.getWalletById(userId, walletId);

        assertEquals(walletId, response.getId());
        assertEquals("Main Wallet", response.getName());
    }

    @Test
    void getWalletById_shouldThrow_whenNotFound() {
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> walletService.getWalletById(userId, walletId));
    }

    @Test
    void getWalletsByUser_shouldReturnList() {
        when(walletRepository.findByUserId(userId)).thenReturn(List.of(wallet));

        List<WalletResponse> responses = walletService.getWalletsByUser(userId);

        assertEquals(1, responses.size());
        assertEquals("Main Wallet", responses.get(0).getName());
    }

    @Test
    void getWalletsByUser_shouldReturnEmptyList_whenNoWallets() {
        when(walletRepository.findByUserId(userId)).thenReturn(List.of());

        List<WalletResponse> responses = walletService.getWalletsByUser(userId);

        assertTrue(responses.isEmpty());
    }

    @Test
    void updateWallet_shouldReturnUpdatedWallet() {
        UpdateWalletRequest request = new UpdateWalletRequest();
        request.setName("Savings Wallet");

        Wallet updatedWallet = new Wallet();
        updatedWallet.setId(walletId);
        updatedWallet.setUserId(userId);
        updatedWallet.setName("Savings Wallet");
        updatedWallet.setCurrency("USD");
        updatedWallet.setBalance(BigDecimal.valueOf(1000));

        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(updatedWallet);

        WalletResponse response = walletService.updateWallet(userId, walletId, request);

        assertEquals("Savings Wallet", response.getName());
    }

    @Test
    void updateWallet_shouldThrow_whenNotFound() {
        UpdateWalletRequest request = new UpdateWalletRequest();
        request.setName("Savings Wallet");

        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> walletService.updateWallet(userId, walletId, request));
    }

    @Test
    void deleteWallet_shouldSucceed() {
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.of(wallet));

        walletService.deleteWallet(userId, walletId);

        verify(walletRepository).delete(wallet);
    }

    @Test
    void deleteWallet_shouldThrow_whenNotFound() {
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> walletService.deleteWallet(userId, walletId));
    }
}
