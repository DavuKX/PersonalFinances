package com.davukx.analyticsservice.infrastructure.persistence.adapter;

import com.davukx.analyticsservice.domain.model.CategorySummary;
import com.davukx.analyticsservice.domain.model.TransactionType;
import com.davukx.analyticsservice.domain.port.CategorySummaryRepository;
import com.davukx.analyticsservice.infrastructure.persistence.mapper.CategorySummaryJpaMapper;
import com.davukx.analyticsservice.infrastructure.persistence.repository.CategorySummaryJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CategorySummaryRepositoryAdapter implements CategorySummaryRepository {

    private final CategorySummaryJpaRepository jpaRepository;

    public CategorySummaryRepositoryAdapter(CategorySummaryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public CategorySummary save(CategorySummary summary) {
        return CategorySummaryJpaMapper.toDomain(jpaRepository.save(CategorySummaryJpaMapper.toEntity(summary)));
    }

    @Override
    public Optional<CategorySummary> findUnique(UUID userId, UUID walletId, UUID categoryId, int year, int month, TransactionType type) {
        return jpaRepository.findUnique(userId, walletId, categoryId, year, month, type)
                .map(CategorySummaryJpaMapper::toDomain);
    }

    @Override
    public List<CategorySummary> findByUserIdAndWalletIdAndYearAndMonth(UUID userId, UUID walletId, int year, int month) {
        return jpaRepository.findByUserIdAndWalletIdAndYearAndMonth(userId, walletId, year, month).stream()
                .map(CategorySummaryJpaMapper::toDomain).toList();
    }

    @Override
    public List<CategorySummary> findByUserIdAndYearAndMonth(UUID userId, int year, int month) {
        return jpaRepository.findByUserIdAndYearAndMonth(userId, year, month).stream()
                .map(CategorySummaryJpaMapper::toDomain).toList();
    }

    @Override
    public List<CategorySummary> findByUserIdAndWalletIdAndTypeAndPeriod(UUID userId, UUID walletId, TransactionType type, int fromYear, int fromMonth, int toYear, int toMonth) {
        return jpaRepository.findByUserIdAndWalletIdAndTypeAndPeriod(userId, walletId, type, fromYear, fromMonth, toYear, toMonth).stream()
                .map(CategorySummaryJpaMapper::toDomain).toList();
    }

    @Override
    public List<CategorySummary> findByUserIdAndTypeAndPeriod(UUID userId, TransactionType type, int fromYear, int fromMonth, int toYear, int toMonth) {
        return jpaRepository.findByUserIdAndTypeAndPeriod(userId, type, fromYear, fromMonth, toYear, toMonth).stream()
                .map(CategorySummaryJpaMapper::toDomain).toList();
    }
}

