package com.personalfinance.transactionservice.infrastructure.persistence.mapper;

import com.personalfinance.transactionservice.domain.model.Category;
import com.personalfinance.transactionservice.infrastructure.persistence.entity.CategoryJpaEntity;

public class CategoryJpaMapper {

    private CategoryJpaMapper() {}

    public static CategoryJpaEntity toEntity(Category c) {
        CategoryJpaEntity e = new CategoryJpaEntity();
        e.setId(c.getId());
        e.setUserId(c.getUserId());
        e.setName(c.getName());
        e.setParentId(c.getParentId());
        e.setTransactionType(c.getTransactionType());
        e.setCreatedAt(c.getCreatedAt());
        return e;
    }

    public static Category toDomain(CategoryJpaEntity e) {
        return new Category(e.getId(), e.getUserId(), e.getName(),
                e.getParentId(), e.getTransactionType(), e.getCreatedAt());
    }
}

