package com.personalfinance.walletservice.application.service;

import com.personalfinance.walletservice.application.dto.CreateWalletCommand;
import com.personalfinance.walletservice.application.dto.UpdateWalletCommand;
import com.personalfinance.walletservice.application.dto.WalletDto;
import com.personalfinance.walletservice.domain.exception.WalletNotFoundException;
import com.personalfinance.walletservice.domain.model.Wallet;
import com.personalfinance.walletservice.domain.port.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletApplicationServiceTest {

    @Mock private WalletRepository walletRepository;

    @InjectMocks
    private WalletApplicationService walletService;

    private UUID userId;
    private UUID walletId;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        walletId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        wallet = new Wallet(walletId, userId, "Main Wallet", "USD", BigDecimal.valueOf(1000), now, now);
    }

    @Test
    void create_returnsWalletDto_withUpperCaseCurrency() {
        when(walletRepository.save(any())).thenReturn(wallet);

        WalletDto dto = walletService.create(userId, new CreateWalletCommand("Main Wallet", "usd", BigDecimal.valueOf(1000)));

        assertEquals("Main Wallet", dto.name());
        assertEquals("USD", dto.currency());
        assertEquals(BigDecimal.valueOf(1000), dto.balance());
        assertEquals(userId, dto.userId());
    }

    @Test
    void create_defaultsBalanceToZero_whenNullProvided() {
        Wallet zeroWallet = new Wallet(walletId, userId, "Empty", "EUR", BigDecimal.ZERO, OffsetDateTime.now(), OffsetDateTime.now());
        when(walletRepository.save(any())).thenReturn(zeroWallet);

        WalletDto dto = walletService.create(userId, new CreateWalletCommand("Empty", "eur", null));

        assertEquals(BigDecimal.ZERO, dto.balance());
    }

    @Test
    void getById_returnsWalletDto_whenFound() {
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.of(wallet));

        WalletDto dto = walletService.getById(userId, walletId);

        assertEquals(walletId, dto.id());
        assertEquals("Main Wallet", dto.name());
    }

    @Test
    void getById_throws_whenNotFound() {
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () -> walletService.getById(userId, walletId));
    }

    @Test
    void getAllByUser_returnsList() {
        when(walletRepository.findByUserId(userId)).thenReturn(List.of(wallet));

        List<WalletDto> dtos = walletService.getAllByUser(userId);

        assertEquals(1, dtos.size());
        assertEquals("Main Wallet", dtos.get(0).name());
    }

    @Test
    void getAllByUser_returnsEmptyList_whenNoWallets() {
        when(walletRepository.findByUserId(userId)).thenReturn(List.of());

        assertTrue(walletService.getAllByUser(userId).isEmpty());
    }

    @Test
    void update_returnsUpdatedDto_whenFound() {
        Wallet updated = new Wallet(walletId, userId, "Savings", "USD", BigDecimal.valueOf(1000), wallet.getCreatedAt(), OffsetDateTime.now());
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any())).thenReturn(updated);

        WalletDto dto = walletService.update(userId, walletId, new UpdateWalletCommand("Savings"));

        assertEquals("Savings", dto.name());
    }

    @Test
    void update_throws_whenNotFound() {
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class,
                () -> walletService.update(userId, walletId, new UpdateWalletCommand("Savings")));
    }

    @Test
    void delete_succeeds_whenFound() {
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.of(wallet));

        walletService.delete(userId, walletId);

        verify(walletRepository).delete(wallet);
    }

    @Test
    void delete_throws_whenNotFound() {
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () -> walletService.delete(userId, walletId));
    }
}
