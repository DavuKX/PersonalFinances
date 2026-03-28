package com.davukx.analyticsservice.infrastructure.persistence.mapper;

import com.davukx.analyticsservice.domain.model.CategorySummary;
import com.davukx.analyticsservice.infrastructure.persistence.entity.CategorySummaryJpaEntity;

public class CategorySummaryJpaMapper {

    private CategorySummaryJpaMapper() {}

    public static CategorySummaryJpaEntity toEntity(CategorySummary s) {
        CategorySummaryJpaEntity e = new CategorySummaryJpaEntity();
        e.setId(s.getId());
        e.setUserId(s.getUserId());
        e.setWalletId(s.getWalletId());
        e.setCategoryId(s.getCategoryId());
        e.setYear(s.getYear());
        e.setMonth(s.getMonth());
        e.setTransactionType(s.getTransactionType());
        e.setTotalAmount(s.getTotalAmount());
        e.setTransactionCount(s.getTransactionCount());
        e.setUpdatedAt(s.getUpdatedAt());
        return e;
    }

    public static CategorySummary toDomain(CategorySummaryJpaEntity e) {
        return new CategorySummary(e.getId(), e.getUserId(), e.getWalletId(),
                e.getCategoryId(), e.getYear(), e.getMonth(), e.getTransactionType(),
                e.getTotalAmount(), e.getTransactionCount(), e.getUpdatedAt());
    }
}

