package com.personalfinance.transactionservice.infrastructure.persistence.repository;

import com.personalfinance.transactionservice.domain.model.TransactionType;
import com.personalfinance.transactionservice.infrastructure.persistence.entity.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, UUID> {

    @Query("SELECT c FROM CategoryJpaEntity c WHERE c.userId IS NULL OR c.userId = :userId")
    List<CategoryJpaEntity> findAllAccessibleByUser(@Param("userId") UUID userId);

    @Query("SELECT c FROM CategoryJpaEntity c WHERE (c.userId IS NULL OR c.userId = :userId) AND c.transactionType = :type")
    List<CategoryJpaEntity> findAllAccessibleByUserAndType(@Param("userId") UUID userId, @Param("type") TransactionType type);

    List<CategoryJpaEntity> findByParentId(UUID parentId);

    boolean existsByNameAndParentIdAndUserIdAndTransactionType(String name, UUID parentId, UUID userId, TransactionType transactionType);

    long countByUserIdIsNull();
}

