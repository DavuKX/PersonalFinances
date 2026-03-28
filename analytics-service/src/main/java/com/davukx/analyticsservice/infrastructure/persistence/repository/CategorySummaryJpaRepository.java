package com.davukx.analyticsservice.infrastructure.persistence.repository;

import com.davukx.analyticsservice.domain.model.TransactionType;
import com.davukx.analyticsservice.infrastructure.persistence.entity.CategorySummaryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategorySummaryJpaRepository extends JpaRepository<CategorySummaryJpaEntity, UUID> {

    @Query("SELECT c FROM CategorySummaryJpaEntity c WHERE c.userId = :userId AND c.walletId = :walletId " +
            "AND (:categoryId IS NULL AND c.categoryId IS NULL OR c.categoryId = :categoryId) " +
            "AND c.year = :year AND c.month = :month AND c.transactionType = :type")
    Optional<CategorySummaryJpaEntity> findUnique(
            @Param("userId") UUID userId, @Param("walletId") UUID walletId,
            @Param("categoryId") UUID categoryId, @Param("year") int year,
            @Param("month") int month, @Param("type") TransactionType type);

    List<CategorySummaryJpaEntity> findByUserIdAndWalletIdAndYearAndMonth(
            UUID userId, UUID walletId, int year, int month);

    List<CategorySummaryJpaEntity> findByUserIdAndYearAndMonth(UUID userId, int year, int month);

    @Query("SELECT c FROM CategorySummaryJpaEntity c WHERE c.userId = :userId AND c.walletId = :walletId " +
            "AND c.transactionType = :type " +
            "AND (c.year * 12 + c.month) >= (:fromYear * 12 + :fromMonth) " +
            "AND (c.year * 12 + c.month) <= (:toYear * 12 + :toMonth) " +
            "ORDER BY c.year ASC, c.month ASC")
    List<CategorySummaryJpaEntity> findByUserIdAndWalletIdAndTypeAndPeriod(
            @Param("userId") UUID userId, @Param("walletId") UUID walletId,
            @Param("type") TransactionType type,
            @Param("fromYear") int fromYear, @Param("fromMonth") int fromMonth,
            @Param("toYear") int toYear, @Param("toMonth") int toMonth);

    @Query("SELECT c FROM CategorySummaryJpaEntity c WHERE c.userId = :userId " +
            "AND c.transactionType = :type " +
            "AND (c.year * 12 + c.month) >= (:fromYear * 12 + :fromMonth) " +
            "AND (c.year * 12 + c.month) <= (:toYear * 12 + :toMonth) " +
            "ORDER BY c.year ASC, c.month ASC")
    List<CategorySummaryJpaEntity> findByUserIdAndTypeAndPeriod(
            @Param("userId") UUID userId, @Param("type") TransactionType type,
            @Param("fromYear") int fromYear, @Param("fromMonth") int fromMonth,
            @Param("toYear") int toYear, @Param("toMonth") int toMonth);
}

