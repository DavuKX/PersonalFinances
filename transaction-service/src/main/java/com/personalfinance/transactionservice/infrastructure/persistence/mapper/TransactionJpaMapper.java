package com.personalfinance.transactionservice.infrastructure.persistence.mapper;

import com.personalfinance.transactionservice.domain.model.Transaction;
import com.personalfinance.transactionservice.infrastructure.persistence.entity.TransactionJpaEntity;

public class TransactionJpaMapper {

    private TransactionJpaMapper() {}

    public static TransactionJpaEntity toEntity(Transaction t) {
        TransactionJpaEntity e = new TransactionJpaEntity();
        e.setId(t.getId());
        e.setUserId(t.getUserId());
        e.setWalletId(t.getWalletId());
        e.setType(t.getType());
        e.setAmount(t.getAmount());
        e.setCurrency(t.getCurrency());
        e.setCategory(t.getCategory());
        e.setSubCategory(t.getSubCategory());
        e.setDescription(t.getDescription());
        e.setTransactionDate(t.getTransactionDate());
        e.setCreatedAt(t.getCreatedAt());
        e.setUpdatedAt(t.getUpdatedAt());
        return e;
    }

    public static Transaction toDomain(TransactionJpaEntity e) {
        return new Transaction(e.getId(), e.getUserId(), e.getWalletId(), e.getType(), e.getAmount(),
                e.getCurrency(), e.getCategory(), e.getSubCategory(), e.getDescription(),
                e.getTransactionDate(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
