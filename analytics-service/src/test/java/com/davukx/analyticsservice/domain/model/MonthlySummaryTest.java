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
        assertThat(summary.getNetSavings()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(summary.getSavingsRate()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(summary.getTransactionCount()).isEqualTo(1);
    }

    @Test
    void applyCreatedExpense() {
        MonthlySummary summary = MonthlySummary.empty(UUID.randomUUID(), UUID.randomUUID(), 2026, 3);
        summary.applyCreated(TransactionType.INCOME, BigDecimal.valueOf(1000));
        summary.applyCreated(TransactionType.EXPENSE, BigDecimal.valueOf(600));

        assertThat(summary.getTotalExpenses()).isEqualByComparingTo(BigDecimal.valueOf(600));
        assertThat(summary.getNetSavings()).isEqualByComparingTo(BigDecimal.valueOf(400));
        assertThat(summary.getSavingsRate()).isEqualByComparingTo(BigDecimal.valueOf(40));
        assertThat(summary.getTransactionCount()).isEqualTo(2);
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
}

