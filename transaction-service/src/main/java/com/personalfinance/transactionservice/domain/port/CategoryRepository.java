package com.personalfinance.transactionservice.domain.port;

import com.personalfinance.transactionservice.domain.model.Category;
import com.personalfinance.transactionservice.domain.model.TransactionType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {
    Category save(Category category);
    Optional<Category> findById(UUID id);
    List<Category> findAllAccessibleByUser(UUID userId);
    List<Category> findAllAccessibleByUserAndType(UUID userId, TransactionType transactionType);
    List<Category> findByParentId(UUID parentId);
    boolean existsByNameAndParentIdAndUserIdAndTransactionType(String name, UUID parentId, UUID userId, TransactionType transactionType);
    void deleteById(UUID id);
    long countByUserIdIsNull();
}

