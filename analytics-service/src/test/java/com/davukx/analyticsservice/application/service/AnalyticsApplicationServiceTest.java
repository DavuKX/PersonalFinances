package com.davukx.analyticsservice.application.service;

import com.davukx.analyticsservice.application.dto.*;
import com.davukx.analyticsservice.domain.model.CategorySummary;
import com.davukx.analyticsservice.domain.model.MonthlySummary;
import com.davukx.analyticsservice.domain.model.TransactionType;
import com.davukx.analyticsservice.domain.port.CategorySummaryRepository;
import com.davukx.analyticsservice.domain.port.MonthlySummaryRepository;
import com.davukx.analyticsservice.infrastructure.messaging.event.TransactionCreatedEvent;
import com.davukx.analyticsservice.infrastructure.messaging.event.TransactionDeletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsApplicationServiceTest {

    @Mock
    private MonthlySummaryRepository monthlySummaryRepository;

    @Mock
    private CategorySummaryRepository categorySummaryRepository;

    private AnalyticsApplicationService service;

    private final UUID userId = UUID.randomUUID();
    private final UUID walletId = UUID.randomUUID();
    private final UUID categoryId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new AnalyticsApplicationService(monthlySummaryRepository, categorySummaryRepository);
    }

    private TransactionCreatedEvent createdEvent(TransactionType type, BigDecimal amount, UUID catId, OffsetDateTime date) {
        TransactionCreatedEvent event = new TransactionCreatedEvent();
        event.setTransactionId(UUID.randomUUID());
        event.setUserId(userId);
        event.setWalletId(walletId);
        event.setType(type);
        event.setAmount(amount);
        event.setCurrency("USD");
        event.setCategoryId(catId);
        event.setTransactionDate(date);
        return event;
    }

    private TransactionDeletedEvent deletedEvent(TransactionType type, BigDecimal amount, UUID catId, OffsetDateTime date) {
        TransactionDeletedEvent event = new TransactionDeletedEvent();
        event.setTransactionId(UUID.randomUUID());
        event.setUserId(userId);
        event.setWalletId(walletId);
        event.setType(type);
        event.setAmount(amount);
        event.setCurrency("USD");
        event.setCategoryId(catId);
        event.setTransactionDate(date);
        return event;
    }

    @Test
    void processCreatedCreatesNewMonthlySummary() {
        OffsetDateTime date = OffsetDateTime.now().withYear(2026).withMonth(3).withDayOfMonth(15);
        when(monthlySummaryRepository.findByUserIdAndWalletIdAndYearAndMonth(userId, walletId, 2026, 3))
                .thenReturn(Optional.empty());
        when(monthlySummaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(categorySummaryRepository.findUnique(any(), any(), any(), anyInt(), anyInt(), any()))
                .thenReturn(Optional.empty());
        when(categorySummaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.processTransactionCreated(createdEvent(TransactionType.INCOME, BigDecimal.valueOf(500), categoryId, date));

        ArgumentCaptor<MonthlySummary> captor = ArgumentCaptor.forClass(MonthlySummary.class);
        verify(monthlySummaryRepository).save(captor.capture());
        assertThat(captor.getValue().getTotalIncome()).isEqualByComparingTo(BigDecimal.valueOf(500));
        assertThat(captor.getValue().getYear()).isEqualTo(2026);
        assertThat(captor.getValue().getMonth()).isEqualTo(3);
    }

    @Test
    void processCreatedUpdatesExistingMonthlySummary() {
        OffsetDateTime date = OffsetDateTime.now().withYear(2026).withMonth(3).withDayOfMonth(15);
        MonthlySummary existing = MonthlySummary.empty(userId, walletId, 2026, 3);
        existing.applyCreated(TransactionType.INCOME, BigDecimal.valueOf(200));

        when(monthlySummaryRepository.findByUserIdAndWalletIdAndYearAndMonth(userId, walletId, 2026, 3))
                .thenReturn(Optional.of(existing));
        when(monthlySummaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(categorySummaryRepository.findUnique(any(), any(), any(), anyInt(), anyInt(), any()))
                .thenReturn(Optional.empty());
        when(categorySummaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.processTransactionCreated(createdEvent(TransactionType.INCOME, BigDecimal.valueOf(300), null, date));

        ArgumentCaptor<MonthlySummary> captor = ArgumentCaptor.forClass(MonthlySummary.class);
        verify(monthlySummaryRepository).save(captor.capture());
        assertThat(captor.getValue().getTotalIncome()).isEqualByComparingTo(BigDecimal.valueOf(500));
        assertThat(captor.getValue().getTransactionCount()).isEqualTo(2);
    }

    @Test
    void processCreatedUsesFallbackDateWhenNull() {
        TransactionCreatedEvent event = createdEvent(TransactionType.EXPENSE, BigDecimal.TEN, null, null);
        when(monthlySummaryRepository.findByUserIdAndWalletIdAndYearAndMonth(eq(userId), eq(walletId), anyInt(), anyInt()))
                .thenReturn(Optional.empty());
        when(monthlySummaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(categorySummaryRepository.findUnique(any(), any(), any(), anyInt(), anyInt(), any()))
                .thenReturn(Optional.empty());
        when(categorySummaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.processTransactionCreated(event);

        verify(monthlySummaryRepository).save(any());
    }

    @Test
    void processCreatedSavesCategorySummary() {
        OffsetDateTime date = OffsetDateTime.now().withYear(2026).withMonth(3).withDayOfMonth(1);
        when(monthlySummaryRepository.findByUserIdAndWalletIdAndYearAndMonth(any(), any(), anyInt(), anyInt()))
                .thenReturn(Optional.empty());
        when(monthlySummaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(categorySummaryRepository.findUnique(userId, walletId, categoryId, 2026, 3, TransactionType.EXPENSE))
                .thenReturn(Optional.empty());
        when(categorySummaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.processTransactionCreated(createdEvent(TransactionType.EXPENSE, BigDecimal.valueOf(150), categoryId, date));

        ArgumentCaptor<CategorySummary> captor = ArgumentCaptor.forClass(CategorySummary.class);
        verify(categorySummaryRepository).save(captor.capture());
        assertThat(captor.getValue().getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(150));
        assertThat(captor.getValue().getCategoryId()).isEqualTo(categoryId);
    }

    @Test
    void processDeletedReversesMonthlySummary() {
        OffsetDateTime date = OffsetDateTime.now().withYear(2026).withMonth(3).withDayOfMonth(1);
        MonthlySummary existing = MonthlySummary.empty(userId, walletId, 2026, 3);
        existing.applyCreated(TransactionType.EXPENSE, BigDecimal.valueOf(400));

        when(monthlySummaryRepository.findByUserIdAndWalletIdAndYearAndMonth(userId, walletId, 2026, 3))
                .thenReturn(Optional.of(existing));
        when(monthlySummaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(categorySummaryRepository.findUnique(any(), any(), any(), anyInt(), anyInt(), any()))
                .thenReturn(Optional.empty());

        service.processTransactionDeleted(deletedEvent(TransactionType.EXPENSE, BigDecimal.valueOf(400), null, date));

        ArgumentCaptor<MonthlySummary> captor = ArgumentCaptor.forClass(MonthlySummary.class);
        verify(monthlySummaryRepository).save(captor.capture());
        assertThat(captor.getValue().getTotalExpenses()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void processDeletedSkipsWhenNoSummaryExists() {
        OffsetDateTime date = OffsetDateTime.now().withYear(2026).withMonth(3).withDayOfMonth(1);
        when(monthlySummaryRepository.findByUserIdAndWalletIdAndYearAndMonth(userId, walletId, 2026, 3))
                .thenReturn(Optional.empty());
        when(categorySummaryRepository.findUnique(any(), any(), any(), anyInt(), anyInt(), any()))
                .thenReturn(Optional.empty());

        service.processTransactionDeleted(deletedEvent(TransactionType.INCOME, BigDecimal.TEN, null, date));

        verify(monthlySummaryRepository, never()).save(any());
    }

    @Test
    void getMonthlyForSpecificWallet() {
        MonthlySummary summary = MonthlySummary.empty(userId, walletId, 2026, 3);
        summary.applyCreated(TransactionType.INCOME, BigDecimal.valueOf(2000));
        summary.applyCreated(TransactionType.EXPENSE, BigDecimal.valueOf(800));
        when(monthlySummaryRepository.findByUserIdAndWalletIdAndYearAndMonth(userId, walletId, 2026, 3))
                .thenReturn(Optional.of(summary));

        MonthlyAnalyticsDto result = service.getMonthly(userId, walletId, 2026, 3);

        assertThat(result.totalIncome()).isEqualByComparingTo(BigDecimal.valueOf(2000));
        assertThat(result.totalExpenses()).isEqualByComparingTo(BigDecimal.valueOf(800));
        assertThat(result.netSavings()).isEqualByComparingTo(BigDecimal.valueOf(1200));
        assertThat(result.savingsRate()).isEqualByComparingTo(BigDecimal.valueOf(60));
    }

    @Test
    void getMonthlyReturnsEmptyWhenNoData() {
        when(monthlySummaryRepository.findByUserIdAndWalletIdAndYearAndMonth(userId, walletId, 2026, 3))
                .thenReturn(Optional.empty());

        MonthlyAnalyticsDto result = service.getMonthly(userId, walletId, 2026, 3);

        assertThat(result.totalIncome()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.totalExpenses()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.savingsRate()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getMonthlyAggregatesAcrossWallets() {
        UUID wallet2 = UUID.randomUUID();
        MonthlySummary s1 = MonthlySummary.empty(userId, walletId, 2026, 3);
        s1.applyCreated(TransactionType.INCOME, BigDecimal.valueOf(1000));
        MonthlySummary s2 = MonthlySummary.empty(userId, wallet2, 2026, 3);
        s2.applyCreated(TransactionType.INCOME, BigDecimal.valueOf(500));
        when(monthlySummaryRepository.findByUserIdAndYearAndMonth(userId, 2026, 3))
                .thenReturn(List.of(s1, s2));

        MonthlyAnalyticsDto result = service.getMonthly(userId, null, 2026, 3);

        assertThat(result.totalIncome()).isEqualByComparingTo(BigDecimal.valueOf(1500));
        assertThat(result.walletId()).isNull();
    }

    @Test
    void getByCategoryFiltersAndSortsDescending() {
        UUID cat1 = UUID.randomUUID();
        UUID cat2 = UUID.randomUUID();
        CategorySummary cs1 = CategorySummary.empty(userId, walletId, cat1, 2026, 3, TransactionType.EXPENSE);
        cs1.applyCreated(BigDecimal.valueOf(300));
        CategorySummary cs2 = CategorySummary.empty(userId, walletId, cat2, 2026, 3, TransactionType.EXPENSE);
        cs2.applyCreated(BigDecimal.valueOf(700));
        when(categorySummaryRepository.findByUserIdAndWalletIdAndYearAndMonth(userId, walletId, 2026, 3))
                .thenReturn(List.of(cs1, cs2));

        List<CategoryAnalyticsDto> result = service.getByCategory(userId, walletId, 2026, 3, TransactionType.EXPENSE);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(700));
        assertThat(result.get(1).totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(300));
    }

    @Test
    void getByCategoryFiltersType() {
        CategorySummary expense = CategorySummary.empty(userId, walletId, categoryId, 2026, 3, TransactionType.EXPENSE);
        expense.applyCreated(BigDecimal.valueOf(200));
        CategorySummary income = CategorySummary.empty(userId, walletId, categoryId, 2026, 3, TransactionType.INCOME);
        income.applyCreated(BigDecimal.valueOf(900));
        when(categorySummaryRepository.findByUserIdAndWalletIdAndYearAndMonth(userId, walletId, 2026, 3))
                .thenReturn(List.of(expense, income));

        List<CategoryAnalyticsDto> result = service.getByCategory(userId, walletId, 2026, 3, TransactionType.EXPENSE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).transactionType()).isEqualTo(TransactionType.EXPENSE);
    }

    @Test
    void getSavingsRateDelegate() {
        MonthlySummary summary = MonthlySummary.empty(userId, walletId, 2026, 3);
        summary.applyCreated(TransactionType.INCOME, BigDecimal.valueOf(1000));
        summary.applyCreated(TransactionType.EXPENSE, BigDecimal.valueOf(250));
        when(monthlySummaryRepository.findByUserIdAndWalletIdAndYearAndMonth(userId, walletId, 2026, 3))
                .thenReturn(Optional.of(summary));

        SavingsRateDto result = service.getSavingsRate(userId, walletId, 2026, 3);

        assertThat(result.savingsRate()).isEqualByComparingTo(BigDecimal.valueOf(75));
        assertThat(result.netSavings()).isEqualByComparingTo(BigDecimal.valueOf(750));
    }

    @Test
    void getTrendReturnsSortedMonthsIncludingEmpty() {
        when(monthlySummaryRepository.findByUserIdAndWalletIdAndPeriod(eq(userId), eq(walletId),
                anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of());

        List<TrendPointDto> result = service.getTrend(userId, walletId, 3);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).totalIncome()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getTrendAggregatesAcrossWalletsWhenNoWalletId() {
        when(monthlySummaryRepository.findByUserIdAndPeriod(eq(userId), anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of());

        List<TrendPointDto> result = service.getTrend(userId, null, 6);

        assertThat(result).hasSize(6);
        verify(monthlySummaryRepository, never()).findByUserIdAndWalletIdAndPeriod(any(), any(), anyInt(), anyInt(), anyInt(), anyInt());
    }

    @Test
    void getWalletBreakdown() {
        UUID wallet2 = UUID.randomUUID();
        MonthlySummary s1 = MonthlySummary.empty(userId, walletId, 2026, 3);
        s1.applyCreated(TransactionType.EXPENSE, BigDecimal.valueOf(100));
        MonthlySummary s2 = MonthlySummary.empty(userId, wallet2, 2026, 3);
        s2.applyCreated(TransactionType.INCOME, BigDecimal.valueOf(2000));
        when(monthlySummaryRepository.findByUserIdAndYearAndMonth(userId, 2026, 3))
                .thenReturn(List.of(s1, s2));

        List<WalletBreakdownDto> result = service.getWalletBreakdown(userId, 2026, 3);

        assertThat(result).hasSize(2);
        assertThat(result.stream().map(WalletBreakdownDto::walletId)).contains(walletId, wallet2);
    }
}

