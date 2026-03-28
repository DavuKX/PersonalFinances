package com.davukx.analyticsservice.infrastructure.persistence.mapper;

import com.davukx.analyticsservice.domain.model.MonthlySummary;
import com.davukx.analyticsservice.domain.model.TransactionType;
import com.davukx.analyticsservice.infrastructure.persistence.entity.MonthlySummaryJpaEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MonthlySummaryJpaMapperTest {

    @Test
    void roundTrip() {
        MonthlySummary original = MonthlySummary.empty(UUID.randomUUID(), UUID.randomUUID(), 2026, 3);
        original.applyCreated(TransactionType.INCOME, BigDecimal.valueOf(500));
        original.applyCreated(TransactionType.EXPENSE, BigDecimal.valueOf(200));

        MonthlySummaryJpaEntity entity = MonthlySummaryJpaMapper.toEntity(original);
        MonthlySummary restored = MonthlySummaryJpaMapper.toDomain(entity);

        assertThat(restored.getId()).isEqualTo(original.getId());
        assertThat(restored.getUserId()).isEqualTo(original.getUserId());
        assertThat(restored.getWalletId()).isEqualTo(original.getWalletId());
        assertThat(restored.getYear()).isEqualTo(2026);
        assertThat(restored.getMonth()).isEqualTo(3);
        assertThat(restored.getTotalIncome()).isEqualByComparingTo(BigDecimal.valueOf(500));
        assertThat(restored.getTotalExpenses()).isEqualByComparingTo(BigDecimal.valueOf(200));
        assertThat(restored.getTransactionCount()).isEqualTo(2);
    }
}

