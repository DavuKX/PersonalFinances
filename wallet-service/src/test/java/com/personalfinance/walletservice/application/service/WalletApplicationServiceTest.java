package com.personalfinance.walletservice.application.service;

import com.personalfinance.walletservice.application.dto.CreateWalletCommand;
import com.personalfinance.walletservice.application.dto.SetSpendingLimitCommand;
import com.personalfinance.walletservice.application.dto.UpdateWalletCommand;
import com.personalfinance.walletservice.application.dto.WalletDto;
import com.personalfinance.walletservice.application.dto.WalletPageDto;
import com.personalfinance.walletservice.application.dto.WalletTotalsDto;
import com.personalfinance.walletservice.domain.exception.WalletArchivedStateException;
import com.personalfinance.walletservice.domain.exception.WalletNotFoundException;
import com.personalfinance.walletservice.domain.model.LimitPeriod;
import com.personalfinance.walletservice.domain.model.SpendingLimit;
import com.personalfinance.walletservice.domain.model.Wallet;
import com.personalfinance.walletservice.domain.port.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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
    private Wallet archivedWallet;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        walletId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        wallet = new Wallet(walletId, userId, "Main Wallet", "USD", BigDecimal.valueOf(1000), null, null, false, null, now, now);
        archivedWallet = new Wallet(walletId, userId, "Old Wallet", "USD", BigDecimal.ZERO, null, null, true, now, now, now);
    }

    @Test
    void create_returnsWalletDto_withUpperCaseCurrency() {
        when(walletRepository.save(any())).thenReturn(wallet);

        WalletDto dto = walletService.create(userId, new CreateWalletCommand("Main Wallet", "usd", BigDecimal.valueOf(1000)));

        assertEquals("USD", dto.currency());
        assertEquals(userId, dto.userId());
    }

    @Test
    void create_defaultsBalanceToZero_whenNullProvided() {
        OffsetDateTime now = OffsetDateTime.now();
        Wallet zeroWallet = new Wallet(walletId, userId, "Empty", "EUR", BigDecimal.ZERO, null, null, false, null, now, now);
        when(walletRepository.save(any())).thenReturn(zeroWallet);

        WalletDto dto = walletService.create(userId, new CreateWalletCommand("Empty", "eur", null));

        assertEquals(BigDecimal.ZERO, dto.balance());
    }

    @Test
    void getById_returnsWalletDto_whenFound() {
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.of(wallet));

        WalletDto dto = walletService.getById(userId, walletId);

        assertEquals(walletId, dto.id());
    }

    @Test
    void getById_throws_whenNotFound() {
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () -> walletService.getById(userId, walletId));
    }

    @Test
    void getAllByUser_returnsList() {
        when(walletRepository.findByUserId(userId)).thenReturn(List.of(wallet));

        assertEquals(1, walletService.getAllByUser(userId).size());
    }

    @Test
    void listByUser_returnsPagedResults() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(walletRepository.findByUserIdAndArchived(userId, false, pageable))
                .thenReturn(new PageImpl<>(List.of(wallet), pageable, 1));

        WalletPageDto page = walletService.listByUser(userId, false, pageable);

        assertEquals(1, page.totalElements());
        assertEquals(1, page.content().size());
        assertFalse(page.content().get(0).archived());
    }

    @Test
    void update_returnsUpdatedDto_whenActiveWallet() {
        OffsetDateTime now = OffsetDateTime.now();
        Wallet updated = new Wallet(walletId, userId, "Savings", "USD", BigDecimal.valueOf(1000), null, null, false, null, now, now);
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any())).thenReturn(updated);

        WalletDto dto = walletService.update(userId, walletId, new UpdateWalletCommand("Savings"));

        assertEquals("Savings", dto.name());
    }

    @Test
    void update_throws_whenWalletIsArchived() {
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.of(archivedWallet));

        assertThrows(WalletArchivedStateException.class,
                () -> walletService.update(userId, walletId, new UpdateWalletCommand("New Name")));
    }

    @Test
    void setSpendingLimit_savesLimitAndReturnsDto() {
        OffsetDateTime now = OffsetDateTime.now();
        Wallet withLimit = new Wallet(walletId, userId, "Main Wallet", "USD", BigDecimal.valueOf(1000),
                new SpendingLimit(BigDecimal.valueOf(500), LimitPeriod.MONTHLY), null, false, null, now, now);
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any())).thenReturn(withLimit);

        WalletDto dto = walletService.setSpendingLimit(userId, walletId,
                new SetSpendingLimitCommand(BigDecimal.valueOf(500), LimitPeriod.MONTHLY));

        assertEquals(BigDecimal.valueOf(500), dto.spendingLimitAmount());
        assertEquals(LimitPeriod.MONTHLY, dto.spendingLimitPeriod());
    }

    @Test
    void setSpendingLimit_throws_whenWalletIsArchived() {
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.of(archivedWallet));

        assertThrows(WalletArchivedStateException.class,
                () -> walletService.setSpendingLimit(userId, walletId,
                        new SetSpendingLimitCommand(BigDecimal.valueOf(500), LimitPeriod.DAILY)));
    }

    @Test
    void removeSpendingLimit_clearsLimitAndReturnsDto() {
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any())).thenReturn(wallet);

        WalletDto dto = walletService.removeSpendingLimit(userId, walletId);

        assertNull(dto.spendingLimitAmount());
        assertNull(dto.spendingLimitPeriod());
    }

    @Test
    void archive_returnsArchivedDto_whenActiveWallet() {
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any())).thenReturn(archivedWallet);

        WalletDto dto = walletService.archive(userId, walletId);

        assertTrue(dto.archived());
    }

    @Test
    void archive_throws_whenAlreadyArchived() {
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.of(archivedWallet));

        assertThrows(WalletArchivedStateException.class, () -> walletService.archive(userId, walletId));
    }

    @Test
    void restore_returnsActiveDto_whenArchivedWallet() {
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.of(archivedWallet));
        when(walletRepository.save(any())).thenReturn(wallet);

        WalletDto dto = walletService.restore(userId, walletId);

        assertFalse(dto.archived());
    }

    @Test
    void restore_throws_whenNotArchived() {
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.of(wallet));

        assertThrows(WalletArchivedStateException.class, () -> walletService.restore(userId, walletId));
    }

    @Test
    void getTotals_sumsByActiveCurrencies() {
        OffsetDateTime now = OffsetDateTime.now();
        Wallet usd1 = new Wallet(UUID.randomUUID(), userId, "W1", "USD", BigDecimal.valueOf(100), null, null, false, null, now, now);
        Wallet usd2 = new Wallet(UUID.randomUUID(), userId, "W2", "USD", BigDecimal.valueOf(200), null, null, false, null, now, now);
        Wallet eur = new Wallet(UUID.randomUUID(), userId, "W3", "EUR", BigDecimal.valueOf(50), null, null, false, null, now, now);
        Wallet archived = new Wallet(UUID.randomUUID(), userId, "W4", "USD", BigDecimal.valueOf(999), null, null, true, now, now, now);
        when(walletRepository.findByUserId(userId)).thenReturn(List.of(usd1, usd2, eur, archived));

        WalletTotalsDto dto = walletService.getTotals(userId);

        assertEquals(2, dto.totals().size());
        dto.totals().forEach(t -> {
            if (t.currency().equals("USD")) assertEquals(BigDecimal.valueOf(300), t.total());
            if (t.currency().equals("EUR")) assertEquals(BigDecimal.valueOf(50), t.total());
        });
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

    @Test
    void adjustBalance_shouldUpdateWalletBalance() {
        Wallet wallet = new Wallet(walletId, userId, "Test Wallet", "USD", BigDecimal.valueOf(100),
                null, null, false, null, OffsetDateTime.now(), OffsetDateTime.now());
        Wallet adjustedWallet = wallet.adjustBalance(BigDecimal.valueOf(50));

        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any())).thenReturn(adjustedWallet);

        walletService.adjustBalance(walletId, userId, BigDecimal.valueOf(50));

        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void adjustBalance_shouldIgnoreArchivedWallet() {
        Wallet archivedWallet = new Wallet(walletId, userId, "Test Wallet", "USD", BigDecimal.valueOf(100),
                null, null, true, OffsetDateTime.now(), OffsetDateTime.now(), OffsetDateTime.now());

        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.of(archivedWallet));

        walletService.adjustBalance(walletId, userId, BigDecimal.valueOf(50));

        verify(walletRepository, never()).save(any());
    }
}
