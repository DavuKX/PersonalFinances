package com.davukx.analyticsservice.infrastructure.persistence.mapper;

import com.davukx.analyticsservice.domain.model.MonthlySummary;
import com.davukx.analyticsservice.infrastructure.persistence.entity.MonthlySummaryJpaEntity;

public class MonthlySummaryJpaMapper {

    private MonthlySummaryJpaMapper() {}

    public static MonthlySummaryJpaEntity toEntity(MonthlySummary s) {
        MonthlySummaryJpaEntity e = new MonthlySummaryJpaEntity();
        e.setId(s.getId());
        e.setUserId(s.getUserId());
        e.setWalletId(s.getWalletId());
        e.setYear(s.getYear());
        e.setMonth(s.getMonth());
        e.setTotalIncome(s.getTotalIncome());
        e.setTotalExpenses(s.getTotalExpenses());
        e.setTransactionCount(s.getTransactionCount());
        e.setUpdatedAt(s.getUpdatedAt());
        return e;
    }

    public static MonthlySummary toDomain(MonthlySummaryJpaEntity e) {
        return new MonthlySummary(e.getId(), e.getUserId(), e.getWalletId(),
                e.getYear(), e.getMonth(), e.getTotalIncome(), e.getTotalExpenses(),
                e.getTransactionCount(), e.getUpdatedAt());
    }
}

