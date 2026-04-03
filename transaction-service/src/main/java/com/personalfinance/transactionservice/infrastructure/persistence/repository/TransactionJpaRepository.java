package com.personalfinance.transactionservice.infrastructure.persistence.repository;

import com.personalfinance.transactionservice.domain.model.TransactionType;
import com.personalfinance.transactionservice.infrastructure.persistence.entity.TransactionJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, UUID>,
        JpaSpecificationExecutor<TransactionJpaEntity> {

    Optional<TransactionJpaEntity> findByIdAndUserId(UUID id, UUID userId);

    Page<TransactionJpaEntity> findByWalletIdAndUserId(UUID walletId, UUID userId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TransactionJpaEntity t " +
           "WHERE t.walletId = :walletId AND t.userId = :userId " +
           "AND t.type IN :types " +
           "AND t.transactionDate >= :from AND t.transactionDate < :to")
    BigDecimal sumAmountByWalletIdAndUserIdAndTypeInAndDateBetween(
            @Param("walletId") UUID walletId,
            @Param("userId") UUID userId,
            @Param("types") List<TransactionType> types,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to);
}
