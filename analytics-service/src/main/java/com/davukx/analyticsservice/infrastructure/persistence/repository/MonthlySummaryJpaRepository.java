package com.davukx.analyticsservice.infrastructure.persistence.repository;

import com.davukx.analyticsservice.infrastructure.persistence.entity.MonthlySummaryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MonthlySummaryJpaRepository extends JpaRepository<MonthlySummaryJpaEntity, UUID> {

    Optional<MonthlySummaryJpaEntity> findByUserIdAndWalletIdAndYearAndMonth(
            UUID userId, UUID walletId, int year, int month);

    List<MonthlySummaryJpaEntity> findByUserIdAndYearAndMonth(UUID userId, int year, int month);

    @Query("SELECT m FROM MonthlySummaryJpaEntity m WHERE m.userId = :userId AND m.walletId = :walletId " +
            "AND (m.year * 12 + m.month) >= (:fromYear * 12 + :fromMonth) " +
            "AND (m.year * 12 + m.month) <= (:toYear * 12 + :toMonth) " +
            "ORDER BY m.year ASC, m.month ASC")
    List<MonthlySummaryJpaEntity> findByUserIdAndWalletIdAndPeriod(
            @Param("userId") UUID userId, @Param("walletId") UUID walletId,
            @Param("fromYear") int fromYear, @Param("fromMonth") int fromMonth,
            @Param("toYear") int toYear, @Param("toMonth") int toMonth);

    @Query("SELECT m FROM MonthlySummaryJpaEntity m WHERE m.userId = :userId " +
            "AND (m.year * 12 + m.month) >= (:fromYear * 12 + :fromMonth) " +
            "AND (m.year * 12 + m.month) <= (:toYear * 12 + :toMonth) " +
            "ORDER BY m.year ASC, m.month ASC")
    List<MonthlySummaryJpaEntity> findByUserIdAndPeriod(
            @Param("userId") UUID userId,
            @Param("fromYear") int fromYear, @Param("fromMonth") int fromMonth,
            @Param("toYear") int toYear, @Param("toMonth") int toMonth);

    List<MonthlySummaryJpaEntity> findByUserIdAndYear(UUID userId, int year);

    List<MonthlySummaryJpaEntity> findByUserIdAndWalletIdAndYear(UUID userId, UUID walletId, int year);
}

