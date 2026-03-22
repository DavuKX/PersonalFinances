package com.personalfinance.transactionservice.infrastructure.persistence.repository;

import com.personalfinance.transactionservice.domain.model.TransactionType;
import com.personalfinance.transactionservice.infrastructure.persistence.entity.TransactionJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, UUID> {

    Optional<TransactionJpaEntity> findByIdAndUserId(UUID id, UUID userId);

    Page<TransactionJpaEntity> findByWalletIdAndUserId(UUID walletId, UUID userId, Pageable pageable);

    @Query("""
            SELECT t FROM TransactionJpaEntity t
            WHERE t.userId = :userId
              AND (:type IS NULL OR t.type = :type)
              AND (:category IS NULL OR t.category = :category)
              AND (:from IS NULL OR t.transactionDate >= :from)
              AND (:to IS NULL OR t.transactionDate <= :to)
            """)
    Page<TransactionJpaEntity> findByUserIdFiltered(
            @Param("userId") UUID userId,
            @Param("type") TransactionType type,
            @Param("category") String category,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to,
            Pageable pageable);
}
