package com.personalfinance.walletservice.infrastructure.persistence.adapter;

import com.personalfinance.walletservice.domain.model.Budget;
import com.personalfinance.walletservice.domain.port.BudgetRepository;
import com.personalfinance.walletservice.infrastructure.persistence.mapper.BudgetJpaMapper;
import com.personalfinance.walletservice.infrastructure.persistence.repository.BudgetJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class BudgetRepositoryAdapter implements BudgetRepository {

    private final BudgetJpaRepository jpaRepository;

    public BudgetRepositoryAdapter(BudgetJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Budget save(Budget budget) {
        return BudgetJpaMapper.toDomain(jpaRepository.save(BudgetJpaMapper.toEntity(budget)));
    }

    @Override
    public Optional<Budget> findById(UUID id) {
        return jpaRepository.findById(id).map(BudgetJpaMapper::toDomain);
    }

    @Override
    public List<Budget> findByWalletIdAndUserId(UUID walletId, UUID userId) {
        return jpaRepository.findByWalletIdAndUserId(walletId, userId)
                .stream().map(BudgetJpaMapper::toDomain).toList();
    }

    @Override
    public Optional<Budget> findByWalletIdAndCategoryId(UUID walletId, UUID categoryId) {
        return jpaRepository.findByWalletIdAndCategoryId(walletId, categoryId)
                .map(BudgetJpaMapper::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteAllByWalletId(UUID walletId) {
        jpaRepository.deleteAllByWalletId(walletId);
    }
}

