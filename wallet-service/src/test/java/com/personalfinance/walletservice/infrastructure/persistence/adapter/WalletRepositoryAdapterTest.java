package com.personalfinance.walletservice.infrastructure.persistence.adapter;

import com.personalfinance.walletservice.domain.model.Wallet;
import com.personalfinance.walletservice.infrastructure.persistence.entity.WalletJpaEntity;
import com.personalfinance.walletservice.infrastructure.persistence.repository.WalletJpaRepository;
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
class WalletRepositoryAdapterTest {

    @Mock private WalletJpaRepository jpaRepository;

    @InjectMocks
    private WalletRepositoryAdapter adapter;

    private UUID walletId;
    private UUID userId;
    private WalletJpaEntity entity;

    @BeforeEach
    void setUp() {
        walletId = UUID.randomUUID();
        userId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        entity = new WalletJpaEntity();
        entity.setId(walletId);
        entity.setUserId(userId);
        entity.setName("Main Wallet");
        entity.setCurrency("USD");
        entity.setBalance(BigDecimal.valueOf(500));
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
    }

    @Test
    void save_returnsMappedDomainWallet() {
        when(jpaRepository.save(any())).thenReturn(entity);
        Wallet wallet = new Wallet(walletId, userId, "Main Wallet", "USD", BigDecimal.valueOf(500), null, false, null, entity.getCreatedAt(), entity.getUpdatedAt());

        Wallet saved = adapter.save(wallet);

        assertEquals(walletId, saved.getId());
        assertEquals("Main Wallet", saved.getName());
    }

    @Test
    void findByIdAndUserId_returnsDomainWallet_whenFound() {
        when(jpaRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.of(entity));

        Optional<Wallet> result = adapter.findByIdAndUserId(walletId, userId);

        assertTrue(result.isPresent());
        assertEquals("Main Wallet", result.get().getName());
    }

    @Test
    void findByIdAndUserId_returnsEmpty_whenNotFound() {
        when(jpaRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.empty());

        assertTrue(adapter.findByIdAndUserId(walletId, userId).isEmpty());
    }

    @Test
    void findByUserId_returnsMappedList() {
        when(jpaRepository.findByUserId(userId)).thenReturn(List.of(entity));

        List<Wallet> wallets = adapter.findByUserId(userId);

        assertEquals(1, wallets.size());
        assertEquals("USD", wallets.get(0).getCurrency());
    }

    @Test
    void delete_callsDeleteById() {
        Wallet wallet = new Wallet(walletId, userId, "Main Wallet", "USD", BigDecimal.ZERO, null, false, null, OffsetDateTime.now(), OffsetDateTime.now());

        adapter.delete(wallet);

        verify(jpaRepository).deleteById(walletId);
    }
}
