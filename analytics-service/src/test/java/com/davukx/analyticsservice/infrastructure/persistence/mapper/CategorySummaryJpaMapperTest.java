package com.davukx.analyticsservice.infrastructure.persistence.mapper;

import com.davukx.analyticsservice.domain.model.CategorySummary;
import com.davukx.analyticsservice.domain.model.TransactionType;
import com.davukx.analyticsservice.infrastructure.persistence.entity.CategorySummaryJpaEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CategorySummaryJpaMapperTest {

    @Test
    void roundTrip() {
        UUID categoryId = UUID.randomUUID();
        CategorySummary original = CategorySummary.empty(UUID.randomUUID(), UUID.randomUUID(),
                categoryId, 2026, 3, TransactionType.EXPENSE);
        original.applyCreated(BigDecimal.valueOf(350));

        CategorySummaryJpaEntity entity = CategorySummaryJpaMapper.toEntity(original);
        CategorySummary restored = CategorySummaryJpaMapper.toDomain(entity);

        assertThat(restored.getId()).isEqualTo(original.getId());
        assertThat(restored.getCategoryId()).isEqualTo(categoryId);
        assertThat(restored.getYear()).isEqualTo(2026);
        assertThat(restored.getMonth()).isEqualTo(3);
        assertThat(restored.getTransactionType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(restored.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(350));
        assertThat(restored.getTransactionCount()).isEqualTo(1);
    }

    @Test
    void roundTripWithNullCategoryId() {
        CategorySummary original = CategorySummary.empty(UUID.randomUUID(), UUID.randomUUID(),
                null, 2026, 3, TransactionType.INCOME);

        CategorySummaryJpaEntity entity = CategorySummaryJpaMapper.toEntity(original);
        CategorySummary restored = CategorySummaryJpaMapper.toDomain(entity);

        assertThat(restored.getCategoryId()).isNull();
    }
}

