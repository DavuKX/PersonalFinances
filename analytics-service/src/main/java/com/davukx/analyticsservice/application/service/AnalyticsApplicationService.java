package com.davukx.analyticsservice.application.service;

import com.davukx.analyticsservice.application.dto.*;
import com.davukx.analyticsservice.application.usecase.AnalyticsUseCase;
import com.davukx.analyticsservice.domain.model.CategorySummary;
import com.davukx.analyticsservice.domain.model.MonthlySummary;
import com.davukx.analyticsservice.domain.model.TransactionType;
import com.davukx.analyticsservice.domain.port.CategorySummaryRepository;
import com.davukx.analyticsservice.domain.port.MonthlySummaryRepository;
import com.davukx.analyticsservice.infrastructure.messaging.event.TransactionCreatedEvent;
import com.davukx.analyticsservice.infrastructure.messaging.event.TransactionDeletedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AnalyticsApplicationService implements AnalyticsUseCase {

    private final MonthlySummaryRepository monthlySummaryRepository;
    private final CategorySummaryRepository categorySummaryRepository;

    public AnalyticsApplicationService(MonthlySummaryRepository monthlySummaryRepository,
                                       CategorySummaryRepository categorySummaryRepository) {
        this.monthlySummaryRepository = monthlySummaryRepository;
        this.categorySummaryRepository = categorySummaryRepository;
    }

    @Override
    @Transactional
    public void processTransactionCreated(TransactionCreatedEvent event) {
        OffsetDateTime txDate = event.getTransactionDate() != null ? event.getTransactionDate() : OffsetDateTime.now();
        int year = txDate.getYear();
        int month = txDate.getMonthValue();

        MonthlySummary summary = monthlySummaryRepository
                .findByUserIdAndWalletIdAndYearAndMonth(event.getUserId(), event.getWalletId(), year, month)
                .orElseGet(() -> MonthlySummary.empty(event.getUserId(), event.getWalletId(), year, month));
        monthlySummaryRepository.save(summary.applyCreated(event.getType(), event.getAmount()));

        categorySummaryRepository.save(
                findOrCreateCategorySummary(event.getUserId(), event.getWalletId(),
                        event.getCategoryId(), year, month, event.getType())
                        .applyCreated(event.getAmount()));
    }

    @Override
    @Transactional
    public void processTransactionDeleted(TransactionDeletedEvent event) {
        OffsetDateTime txDate = event.getTransactionDate() != null ? event.getTransactionDate() : OffsetDateTime.now();
        int year = txDate.getYear();
        int month = txDate.getMonthValue();

        monthlySummaryRepository
                .findByUserIdAndWalletIdAndYearAndMonth(event.getUserId(), event.getWalletId(), year, month)
                .ifPresent(summary -> monthlySummaryRepository.save(
                        summary.applyDeleted(event.getType(), event.getAmount())));

        categorySummaryRepository
                .findUnique(event.getUserId(), event.getWalletId(), event.getCategoryId(), year, month, event.getType())
                .ifPresent(summary -> categorySummaryRepository.save(summary.applyDeleted(event.getAmount())));
    }

    @Override
    public MonthlyAnalyticsDto getMonthly(UUID userId, UUID walletId, int year, int month) {
        if (walletId != null) {
            return monthlySummaryRepository
                    .findByUserIdAndWalletIdAndYearAndMonth(userId, walletId, year, month)
                    .map(this::toMonthlyDto)
                    .orElseGet(() -> emptyMonthlyDto(userId, walletId, year, month));
        }
        List<MonthlySummary> summaries = monthlySummaryRepository.findByUserIdAndYearAndMonth(userId, year, month);
        return aggregateMonthly(userId, null, year, month, summaries);
    }

    @Override
    public List<CategoryAnalyticsDto> getByCategory(UUID userId, UUID walletId, int year, int month, TransactionType type) {
        List<CategorySummary> summaries = walletId != null
                ? categorySummaryRepository.findByUserIdAndWalletIdAndYearAndMonth(userId, walletId, year, month)
                : categorySummaryRepository.findByUserIdAndYearAndMonth(userId, year, month);

        return summaries.stream()
                .filter(s -> type == null || s.getTransactionType() == type)
                .collect(Collectors.groupingBy(
                        s -> new CategoryKey(s.getCategoryId(), s.getTransactionType()),
                        Collectors.toList()))
                .entrySet().stream()
                .map(entry -> {
                    CategoryKey key = entry.getKey();
                    List<CategorySummary> group = entry.getValue();
                    BigDecimal total = group.stream().map(CategorySummary::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    int count = group.stream().mapToInt(CategorySummary::getTransactionCount).sum();
                    return new CategoryAnalyticsDto(key.categoryId(), key.type(), year, month, total, count);
                })
                .sorted((a, b) -> b.totalAmount().compareTo(a.totalAmount()))
                .toList();
    }

    @Override
    public SavingsRateDto getSavingsRate(UUID userId, UUID walletId, int year, int month) {
        MonthlyAnalyticsDto monthly = getMonthly(userId, walletId, year, month);
        return new SavingsRateDto(userId, walletId, year, month,
                monthly.totalIncome(), monthly.totalExpenses(), monthly.totalSavings(),
                monthly.netSavings(), monthly.savingsRate());
    }

    @Override
    public List<TrendPointDto> getTrend(UUID userId, UUID walletId, int months) {
        YearMonth to = YearMonth.now();
        YearMonth from = to.minusMonths(months - 1L);

        List<MonthlySummary> rawSummaries = walletId != null
                ? monthlySummaryRepository.findByUserIdAndWalletIdAndPeriod(userId, walletId,
                        from.getYear(), from.getMonthValue(), to.getYear(), to.getMonthValue())
                : monthlySummaryRepository.findByUserIdAndPeriod(userId,
                        from.getYear(), from.getMonthValue(), to.getYear(), to.getMonthValue());

        Map<String, List<MonthlySummary>> byPeriod = rawSummaries.stream()
                .collect(Collectors.groupingBy(s -> s.getYear() + "-" + s.getMonth()));

        List<TrendPointDto> trend = new ArrayList<>();
        YearMonth cursor = from;
        while (!cursor.isAfter(to)) {
            String key = cursor.getYear() + "-" + cursor.getMonthValue();
            List<MonthlySummary> group = byPeriod.getOrDefault(key, List.of());
            BigDecimal income = group.stream().map(MonthlySummary::getTotalIncome)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal expenses = group.stream().map(MonthlySummary::getTotalExpenses)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal savings = group.stream().map(MonthlySummary::getTotalSavings)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal netSavings = income.subtract(expenses).subtract(savings);
            BigDecimal savingsRate = income.compareTo(BigDecimal.ZERO) > 0
                    ? savings.divide(income, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            int count = group.stream().mapToInt(MonthlySummary::getTransactionCount).sum();
            trend.add(new TrendPointDto(cursor.getYear(), cursor.getMonthValue(),
                    income, expenses, savings, netSavings, savingsRate, count));
            cursor = cursor.plusMonths(1);
        }
        return trend;
    }

    @Override
    public List<WalletBreakdownDto> getWalletBreakdown(UUID userId, int year, int month) {
        return monthlySummaryRepository.findByUserIdAndYearAndMonth(userId, year, month).stream()
                .map(s -> new WalletBreakdownDto(s.getWalletId(), s.getYear(), s.getMonth(),
                        s.getTotalIncome(), s.getTotalExpenses(), s.getTotalSavings(), s.getNetSavings(),
                        s.getSavingsRate(), s.getTransactionCount()))
                .toList();
    }

    private CategorySummary findOrCreateCategorySummary(UUID userId, UUID walletId, UUID categoryId,
                                                         int year, int month, TransactionType type) {
        return categorySummaryRepository
                .findUnique(userId, walletId, categoryId, year, month, type)
                .orElseGet(() -> CategorySummary.empty(userId, walletId, categoryId, year, month, type));
    }

    private MonthlyAnalyticsDto aggregateMonthly(UUID userId, UUID walletId, int year, int month,
                                                  List<MonthlySummary> summaries) {
        BigDecimal income = summaries.stream().map(MonthlySummary::getTotalIncome)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expenses = summaries.stream().map(MonthlySummary::getTotalExpenses)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal savings = summaries.stream().map(MonthlySummary::getTotalSavings)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int count = summaries.stream().mapToInt(MonthlySummary::getTransactionCount).sum();
        BigDecimal netSavings = income.subtract(expenses).subtract(savings);
        BigDecimal savingsRate = income.compareTo(BigDecimal.ZERO) > 0
                ? savings.divide(income, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        return new MonthlyAnalyticsDto(userId, walletId, year, month, income, expenses, savings, netSavings, savingsRate, count);
    }

    private MonthlyAnalyticsDto toMonthlyDto(MonthlySummary s) {
        return new MonthlyAnalyticsDto(s.getUserId(), s.getWalletId(), s.getYear(), s.getMonth(),
                s.getTotalIncome(), s.getTotalExpenses(), s.getTotalSavings(), s.getNetSavings(), s.getSavingsRate(), s.getTransactionCount());
    }

    private MonthlyAnalyticsDto emptyMonthlyDto(UUID userId, UUID walletId, int year, int month) {
        return new MonthlyAnalyticsDto(userId, walletId, year, month,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0);
    }

    private record CategoryKey(UUID categoryId, TransactionType type) {}
}

