package com.personalfinance.walletservice.application.service;

import com.personalfinance.walletservice.application.dto.*;
import com.personalfinance.walletservice.domain.exception.BudgetLimitExceededException;
import com.personalfinance.walletservice.domain.exception.BudgetNotFoundException;
import com.personalfinance.walletservice.domain.exception.WalletArchivedStateException;
import com.personalfinance.walletservice.domain.exception.WalletNotFoundException;
import com.personalfinance.walletservice.domain.model.*;
import com.personalfinance.walletservice.domain.port.BudgetRepository;
import com.personalfinance.walletservice.domain.port.BudgetSpendingRepository;
import com.personalfinance.walletservice.domain.port.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetApplicationServiceTest {

    @Mock private BudgetRepository budgetRepository;
    @Mock private BudgetSpendingRepository budgetSpendingRepository;
    @Mock private WalletRepository walletRepository;

    @InjectMocks
    private BudgetApplicationService service;

    private UUID userId;
    private UUID walletId;
    private UUID categoryId;
    private Wallet activeWallet;
    private Wallet archivedWallet;
    private Budget budget;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        walletId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        activeWallet = new Wallet(walletId, userId, "My Wallet", "USD",
                BigDecimal.valueOf(5000), null, BigDecimal.valueOf(10000), false, null, now, now);
        archivedWallet = new Wallet(walletId, userId, "Old Wallet", "USD",
                BigDecimal.ZERO, null, null, true, now, now, now);
        budget = new Budget(UUID.randomUUID(), walletId, userId, categoryId,
                BudgetType.FIXED, BigDecimal.valueOf(500), BudgetPeriod.MONTHLY, now, now);
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_returnsDto_whenValid() {
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.of(activeWallet));
        when(budgetRepository.findByWalletIdAndCategoryId(walletId, categoryId)).thenReturn(Optional.empty());
        when(budgetRepository.save(any())).thenReturn(budget);

        BudgetDto dto = service.create(walletId, userId,
                new CreateBudgetCommand(categoryId, BudgetType.FIXED, BigDecimal.valueOf(500)));

        assertNotNull(dto);
        assertEquals(categoryId, dto.categoryId());
        assertEquals(BudgetType.FIXED, dto.budgetType());
        assertEquals(BigDecimal.valueOf(500), dto.amount());
    }

    @Test
    void create_throws_whenWalletNotFound() {
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class,
                () -> service.create(walletId, userId,
                        new CreateBudgetCommand(categoryId, BudgetType.FIXED, BigDecimal.valueOf(500))));
    }

    @Test
    void create_throws_whenWalletIsArchived() {
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.of(archivedWallet));

        assertThrows(WalletArchivedStateException.class,
                () -> service.create(walletId, userId,
                        new CreateBudgetCommand(categoryId, BudgetType.FIXED, BigDecimal.valueOf(500))));
    }

    @Test
    void create_throws_whenCategoryAlreadyHasBudget() {
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.of(activeWallet));
        when(budgetRepository.findByWalletIdAndCategoryId(walletId, categoryId))
                .thenReturn(Optional.of(budget));

        assertThrows(IllegalArgumentException.class,
                () -> service.create(walletId, userId,
                        new CreateBudgetCommand(categoryId, BudgetType.FIXED, BigDecimal.valueOf(500))));
    }

    @Test
    void create_throws_whenAmountIsZeroOrNegative() {
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.of(activeWallet));

        assertThrows(IllegalArgumentException.class,
                () -> service.create(walletId, userId,
                        new CreateBudgetCommand(categoryId, BudgetType.FIXED, BigDecimal.ZERO)));
    }

    @Test
    void create_throws_whenPercentageExceeds100() {
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.of(activeWallet));

        assertThrows(IllegalArgumentException.class,
                () -> service.create(walletId, userId,
                        new CreateBudgetCommand(categoryId, BudgetType.PERCENTAGE, BigDecimal.valueOf(101))));
    }

    @Test
    void create_throws_whenCombinedPercentageWouldExceed100() {
        UUID cat2 = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        Budget existingPctBudget = new Budget(UUID.randomUUID(), walletId, userId, categoryId,
                BudgetType.PERCENTAGE, BigDecimal.valueOf(70), BudgetPeriod.MONTHLY, now, now);
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.of(activeWallet));
        when(budgetRepository.findByWalletIdAndCategoryId(walletId, cat2)).thenReturn(Optional.empty());
        when(budgetRepository.findByWalletIdAndUserId(walletId, userId)).thenReturn(List.of(existingPctBudget));

        assertThrows(BudgetLimitExceededException.class,
                () -> service.create(walletId, userId,
                        new CreateBudgetCommand(cat2, BudgetType.PERCENTAGE, BigDecimal.valueOf(40))));
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void update_returnsUpdatedDto_whenValid() {
        UUID budgetId = budget.getId();
        OffsetDateTime now = OffsetDateTime.now();
        Budget updated = new Budget(budgetId, walletId, userId, categoryId,
                BudgetType.FIXED, BigDecimal.valueOf(800), BudgetPeriod.MONTHLY, now, now);
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget));
        when(budgetRepository.save(any())).thenReturn(updated);

        BudgetDto dto = service.update(budgetId, userId,
                new UpdateBudgetCommand(BudgetType.FIXED, BigDecimal.valueOf(800)));

        assertEquals(BigDecimal.valueOf(800), dto.amount());
    }

    @Test
    void update_throws_whenBudgetNotFound() {
        UUID budgetId = UUID.randomUUID();
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.empty());

        assertThrows(BudgetNotFoundException.class,
                () -> service.update(budgetId, userId,
                        new UpdateBudgetCommand(BudgetType.FIXED, BigDecimal.valueOf(800))));
    }

    @Test
    void update_throws_whenOwnedByDifferentUser() {
        UUID differentUser = UUID.randomUUID();
        when(budgetRepository.findById(budget.getId())).thenReturn(Optional.of(budget));

        assertThrows(BudgetNotFoundException.class,
                () -> service.update(budget.getId(), differentUser,
                        new UpdateBudgetCommand(BudgetType.FIXED, BigDecimal.valueOf(800))));
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void delete_succeeds_whenOwnerDeletes() {
        when(budgetRepository.findById(budget.getId())).thenReturn(Optional.of(budget));

        service.delete(budget.getId(), userId);

        verify(budgetRepository).deleteById(budget.getId());
    }

    @Test
    void delete_throws_whenBudgetNotFound() {
        UUID budgetId = UUID.randomUUID();
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.empty());

        assertThrows(BudgetNotFoundException.class, () -> service.delete(budgetId, userId));
    }

    // ── listByWallet ──────────────────────────────────────────────────────────

    @Test
    void listByWallet_returnsSummaryWithSpending() {
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.of(activeWallet));
        when(budgetRepository.findByWalletIdAndUserId(walletId, userId)).thenReturn(List.of(budget));
        when(budgetSpendingRepository.findByWalletIdAndPeriodStart(eq(walletId), any(LocalDate.class)))
                .thenReturn(List.of());

        List<BudgetSummaryDto> summaries = service.listByWallet(walletId, userId);

        assertEquals(1, summaries.size());
        assertEquals(BigDecimal.ZERO, summaries.get(0).spentAmount());
        assertEquals(BigDecimal.valueOf(500), summaries.get(0).resolvedAmount());
        assertEquals(0.0, summaries.get(0).percentUsed());
    }

    @Test
    void listByWallet_computesPercentageResolvedAmount_fromMonthlyIncome() {
        OffsetDateTime now = OffsetDateTime.now();
        Budget pctBudget = new Budget(UUID.randomUUID(), walletId, userId, categoryId,
                BudgetType.PERCENTAGE, BigDecimal.valueOf(50), BudgetPeriod.MONTHLY, now, now);
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.of(activeWallet));
        when(budgetRepository.findByWalletIdAndUserId(walletId, userId)).thenReturn(List.of(pctBudget));
        when(budgetSpendingRepository.findByWalletIdAndPeriodStart(eq(walletId), any(LocalDate.class)))
                .thenReturn(List.of());

        List<BudgetSummaryDto> summaries = service.listByWallet(walletId, userId);

        // 50% of 10,000 income = 5,000
        assertEquals(new BigDecimal("5000.0000"), summaries.get(0).resolvedAmount());
    }

    // ── setBulkBudgets ────────────────────────────────────────────────────────

    @Test
    void setBulkBudgets_replacesAllAndUpdatesIncome() {
        UUID cat1 = UUID.randomUUID();
        UUID cat2 = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        Budget b1 = new Budget(UUID.randomUUID(), walletId, userId, cat1,
                BudgetType.PERCENTAGE, BigDecimal.valueOf(50), BudgetPeriod.MONTHLY, now, now);
        Budget b2 = new Budget(UUID.randomUUID(), walletId, userId, cat2,
                BudgetType.PERCENTAGE, BigDecimal.valueOf(30), BudgetPeriod.MONTHLY, now, now);
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.of(activeWallet));
        when(walletRepository.save(any())).thenReturn(activeWallet);
        when(budgetRepository.save(any())).thenReturn(b1, b2);

        BulkBudgetCommand command = new BulkBudgetCommand(
                BigDecimal.valueOf(10000),
                List.of(
                        new BulkBudgetCommand.Allocation(cat1, BudgetType.PERCENTAGE, BigDecimal.valueOf(50)),
                        new BulkBudgetCommand.Allocation(cat2, BudgetType.PERCENTAGE, BigDecimal.valueOf(30))
                )
        );

        List<BudgetDto> dtos = service.setBulkBudgets(walletId, userId, command);

        verify(budgetRepository).deleteAllByWalletId(walletId);
        assertEquals(2, dtos.size());
    }

    @Test
    void setBulkBudgets_throws_whenTotalPercentageExceeds100() {
        when(walletRepository.findByIdAndUserId(walletId, userId)).thenReturn(Optional.of(activeWallet));

        UUID cat1 = UUID.randomUUID();
        UUID cat2 = UUID.randomUUID();
        BulkBudgetCommand command = new BulkBudgetCommand(
                BigDecimal.valueOf(10000),
                List.of(
                        new BulkBudgetCommand.Allocation(cat1, BudgetType.PERCENTAGE, BigDecimal.valueOf(70)),
                        new BulkBudgetCommand.Allocation(cat2, BudgetType.PERCENTAGE, BigDecimal.valueOf(40))
                )
        );

        assertThrows(BudgetLimitExceededException.class,
                () -> service.setBulkBudgets(walletId, userId, command));
    }
}


