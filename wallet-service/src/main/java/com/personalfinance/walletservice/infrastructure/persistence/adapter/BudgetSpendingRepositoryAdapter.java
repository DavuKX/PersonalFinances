package com.personalfinance.walletservice.infrastructure.persistence.adapter;

import com.personalfinance.walletservice.domain.model.BudgetSpending;
import com.personalfinance.walletservice.domain.port.BudgetSpendingRepository;
import com.personalfinance.walletservice.infrastructure.persistence.mapper.BudgetSpendingJpaMapper;
import com.personalfinance.walletservice.infrastructure.persistence.repository.BudgetSpendingJpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class BudgetSpendingRepositoryAdapter implements BudgetSpendingRepository {

    private final BudgetSpendingJpaRepository jpaRepository;

    public BudgetSpendingRepositoryAdapter(BudgetSpendingJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public BudgetSpending save(BudgetSpending spending) {
        return BudgetSpendingJpaMapper.toDomain(
                jpaRepository.save(BudgetSpendingJpaMapper.toEntity(spending)));
    }

    @Override
    public Optional<BudgetSpending> findByWalletIdAndCategoryIdAndPeriodStart(
            UUID walletId, UUID categoryId, LocalDate periodStart) {
        return jpaRepository.findByWalletIdAndCategoryIdAndPeriodStart(walletId, categoryId, periodStart)
                .map(BudgetSpendingJpaMapper::toDomain);
    }

    @Override
    public List<BudgetSpending> findByWalletIdAndPeriodStart(UUID walletId, LocalDate periodStart) {
        return jpaRepository.findByWalletIdAndPeriodStart(walletId, periodStart)
                .stream().map(BudgetSpendingJpaMapper::toDomain).toList();
    }
}

