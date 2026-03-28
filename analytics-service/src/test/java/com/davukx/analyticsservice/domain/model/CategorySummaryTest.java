package com.davukx.analyticsservice.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CategorySummaryTest {

    @Test
    void emptyStartsAtZero() {
        CategorySummary summary = CategorySummary.empty(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), 2026, 3, TransactionType.EXPENSE);

        assertThat(summary.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.getTransactionCount()).isZero();
    }

    @Test
    void applyCreatedAccumulates() {
        CategorySummary summary = CategorySummary.empty(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), 2026, 3, TransactionType.EXPENSE);
        summary.applyCreated(BigDecimal.valueOf(150));
        summary.applyCreated(BigDecimal.valueOf(75));

        assertThat(summary.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(225));
        assertThat(summary.getTransactionCount()).isEqualTo(2);
    }

    @Test
    void applyDeletedReversesCreated() {
        CategorySummary summary = CategorySummary.empty(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), 2026, 3, TransactionType.EXPENSE);
        summary.applyCreated(BigDecimal.valueOf(150));
        summary.applyDeleted(BigDecimal.valueOf(150));

        assertThat(summary.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.getTransactionCount()).isZero();
    }

    @Test
    void applyDeletedDoesNotGoBelowZero() {
        CategorySummary summary = CategorySummary.empty(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), 2026, 3, TransactionType.EXPENSE);
        summary.applyDeleted(BigDecimal.valueOf(500));

        assertThat(summary.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.getTransactionCount()).isZero();
    }

    @Test
    void nullCategoryIdAllowed() {
        CategorySummary summary = CategorySummary.empty(UUID.randomUUID(), UUID.randomUUID(),
                null, 2026, 3, TransactionType.INCOME);
        assertThat(summary.getCategoryId()).isNull();
    }
}

