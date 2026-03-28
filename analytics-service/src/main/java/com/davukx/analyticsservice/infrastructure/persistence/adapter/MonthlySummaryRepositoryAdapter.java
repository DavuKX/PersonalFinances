package com.davukx.analyticsservice.infrastructure.persistence.adapter;

import com.davukx.analyticsservice.domain.model.MonthlySummary;
import com.davukx.analyticsservice.domain.port.MonthlySummaryRepository;
import com.davukx.analyticsservice.infrastructure.persistence.mapper.MonthlySummaryJpaMapper;
import com.davukx.analyticsservice.infrastructure.persistence.repository.MonthlySummaryJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class MonthlySummaryRepositoryAdapter implements MonthlySummaryRepository {

    private final MonthlySummaryJpaRepository jpaRepository;

    public MonthlySummaryRepositoryAdapter(MonthlySummaryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public MonthlySummary save(MonthlySummary summary) {
        return MonthlySummaryJpaMapper.toDomain(jpaRepository.save(MonthlySummaryJpaMapper.toEntity(summary)));
    }

    @Override
    public Optional<MonthlySummary> findByUserIdAndWalletIdAndYearAndMonth(UUID userId, UUID walletId, int year, int month) {
        return jpaRepository.findByUserIdAndWalletIdAndYearAndMonth(userId, walletId, year, month)
                .map(MonthlySummaryJpaMapper::toDomain);
    }

    @Override
    public List<MonthlySummary> findByUserIdAndYearAndMonth(UUID userId, int year, int month) {
        return jpaRepository.findByUserIdAndYearAndMonth(userId, year, month).stream()
                .map(MonthlySummaryJpaMapper::toDomain).toList();
    }

    @Override
    public List<MonthlySummary> findByUserIdAndWalletIdAndPeriod(UUID userId, UUID walletId, int fromYear, int fromMonth, int toYear, int toMonth) {
        return jpaRepository.findByUserIdAndWalletIdAndPeriod(userId, walletId, fromYear, fromMonth, toYear, toMonth).stream()
                .map(MonthlySummaryJpaMapper::toDomain).toList();
    }

    @Override
    public List<MonthlySummary> findByUserIdAndPeriod(UUID userId, int fromYear, int fromMonth, int toYear, int toMonth) {
        return jpaRepository.findByUserIdAndPeriod(userId, fromYear, fromMonth, toYear, toMonth).stream()
                .map(MonthlySummaryJpaMapper::toDomain).toList();
    }

    @Override
    public List<MonthlySummary> findByUserIdAndYear(UUID userId, int year) {
        return jpaRepository.findByUserIdAndYear(userId, year).stream()
                .map(MonthlySummaryJpaMapper::toDomain).toList();
    }

    @Override
    public List<MonthlySummary> findByUserIdAndWalletIdAndYear(UUID userId, UUID walletId, int year) {
        return jpaRepository.findByUserIdAndWalletIdAndYear(userId, walletId, year).stream()
                .map(MonthlySummaryJpaMapper::toDomain).toList();
    }
}

