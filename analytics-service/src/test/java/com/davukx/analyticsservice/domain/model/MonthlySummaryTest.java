package com.davukx.analyticsservice.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MonthlySummaryTest {

    @Test
    void emptyStartsAtZero() {
        MonthlySummary summary = MonthlySummary.empty(UUID.randomUUID(), UUID.randomUUID(), 2026, 3);
        assertThat(summary.getTotalIncome()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.getTotalExpenses()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.getTotalSavings()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.getNetSavings()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.getSavingsRate()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.getTransactionCount()).isZero();
    }

    @Test
    void applyCreatedIncome() {
        MonthlySummary summary = MonthlySummary.empty(UUID.randomUUID(), UUID.randomUUID(), 2026, 3);
        summary.applyCreated(TransactionType.INCOME, BigDecimal.valueOf(1000));

        assertThat(summary.getTotalIncome()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(summary.getTotalExpenses()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.getTotalSavings()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.getNetSavings()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(summary.getSavingsRate()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.getTransactionCount()).isEqualTo(1);
    }

    @Test
    void applyCreatedExpense() {
        MonthlySummary summary = MonthlySummary.empty(UUID.randomUUID(), UUID.randomUUID(), 2026, 3);
        summary.applyCreated(TransactionType.INCOME, BigDecimal.valueOf(1000));
        summary.applyCreated(TransactionType.EXPENSE, BigDecimal.valueOf(600));

        assertThat(summary.getTotalExpenses()).isEqualByComparingTo(BigDecimal.valueOf(600));
        assertThat(summary.getNetSavings()).isEqualByComparingTo(BigDecimal.valueOf(400));
        assertThat(summary.getSavingsRate()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.getTransactionCount()).isEqualTo(2);
    }

    @Test
    void applyCreatedSavings() {
        MonthlySummary summary = MonthlySummary.empty(UUID.randomUUID(), UUID.randomUUID(), 2026, 3);
        summary.applyCreated(TransactionType.INCOME, BigDecimal.valueOf(1000));
        summary.applyCreated(TransactionType.SAVINGS, BigDecimal.valueOf(200));

        assertThat(summary.getTotalSavings()).isEqualByComparingTo(BigDecimal.valueOf(200));
        assertThat(summary.getNetSavings()).isEqualByComparingTo(BigDecimal.valueOf(800));
        assertThat(summary.getSavingsRate()).isEqualByComparingTo(BigDecimal.valueOf(20));
        assertThat(summary.getTransactionCount()).isEqualTo(2);
    }

    @Test
    void applyCreatedAllTypes() {
        MonthlySummary summary = MonthlySummary.empty(UUID.randomUUID(), UUID.randomUUID(), 2026, 3);
        summary.applyCreated(TransactionType.INCOME, BigDecimal.valueOf(1000));
        summary.applyCreated(TransactionType.EXPENSE, BigDecimal.valueOf(400));
        summary.applyCreated(TransactionType.SAVINGS, BigDecimal.valueOf(300));

        assertThat(summary.getTotalIncome()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(summary.getTotalExpenses()).isEqualByComparingTo(BigDecimal.valueOf(400));
        assertThat(summary.getTotalSavings()).isEqualByComparingTo(BigDecimal.valueOf(300));
        assertThat(summary.getNetSavings()).isEqualByComparingTo(BigDecimal.valueOf(300));
        assertThat(summary.getSavingsRate()).isEqualByComparingTo(BigDecimal.valueOf(30));
        assertThat(summary.getTransactionCount()).isEqualTo(3);
    }

    @Test
    void applyDeletedReversesCreated() {
        MonthlySummary summary = MonthlySummary.empty(UUID.randomUUID(), UUID.randomUUID(), 2026, 3);
        summary.applyCreated(TransactionType.INCOME, BigDecimal.valueOf(1000));
        summary.applyDeleted(TransactionType.INCOME, BigDecimal.valueOf(1000));

        assertThat(summary.getTotalIncome()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.getTransactionCount()).isZero();
        assertThat(summary.getSavingsRate()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void applyDeletedSavingsReversesCreated() {
        MonthlySummary summary = MonthlySummary.empty(UUID.randomUUID(), UUID.randomUUID(), 2026, 3);
        summary.applyCreated(TransactionType.INCOME, BigDecimal.valueOf(1000));
        summary.applyCreated(TransactionType.SAVINGS, BigDecimal.valueOf(300));
        summary.applyDeleted(TransactionType.SAVINGS, BigDecimal.valueOf(300));

        assertThat(summary.getTotalSavings()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.getNetSavings()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(summary.getSavingsRate()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void savingsRateIsZeroWhenNoIncome() {
        MonthlySummary summary = MonthlySummary.empty(UUID.randomUUID(), UUID.randomUUID(), 2026, 3);
        summary.applyCreated(TransactionType.EXPENSE, BigDecimal.valueOf(300));

        assertThat(summary.getSavingsRate()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.getNetSavings()).isEqualByComparingTo(BigDecimal.valueOf(-300));
    }

    @Test
    void applyDeletedDoesNotGoBelowZero() {
        MonthlySummary summary = MonthlySummary.empty(UUID.randomUUID(), UUID.randomUUID(), 2026, 3);
        summary.applyDeleted(TransactionType.INCOME, BigDecimal.valueOf(999));

        assertThat(summary.getTotalIncome()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.getTransactionCount()).isZero();
    }

    @Test
    void applyDeletedSavingsDoesNotGoBelowZero() {
        MonthlySummary summary = MonthlySummary.empty(UUID.randomUUID(), UUID.randomUUID(), 2026, 3);
        summary.applyDeleted(TransactionType.SAVINGS, BigDecimal.valueOf(999));

        assertThat(summary.getTotalSavings()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
