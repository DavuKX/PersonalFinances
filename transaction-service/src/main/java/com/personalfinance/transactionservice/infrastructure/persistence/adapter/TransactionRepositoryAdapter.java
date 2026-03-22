package com.personalfinance.transactionservice.infrastructure.persistence.adapter;

import com.personalfinance.transactionservice.domain.model.Transaction;
import com.personalfinance.transactionservice.domain.model.TransactionType;
import com.personalfinance.transactionservice.domain.port.TransactionRepository;
import com.personalfinance.transactionservice.infrastructure.persistence.mapper.TransactionJpaMapper;
import com.personalfinance.transactionservice.infrastructure.persistence.repository.TransactionJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TransactionRepositoryAdapter implements TransactionRepository {

    private final TransactionJpaRepository jpaRepository;

    public TransactionRepositoryAdapter(TransactionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Transaction save(Transaction transaction) {
        return TransactionJpaMapper.toDomain(jpaRepository.save(TransactionJpaMapper.toEntity(transaction)));
    }

    @Override
    public Optional<Transaction> findByIdAndUserId(UUID id, UUID userId) {
        return jpaRepository.findByIdAndUserId(id, userId).map(TransactionJpaMapper::toDomain);
    }

    @Override
    public Page<Transaction> findByWalletIdAndUserId(UUID walletId, UUID userId, Pageable pageable) {
        return jpaRepository.findByWalletIdAndUserId(walletId, userId, pageable).map(TransactionJpaMapper::toDomain);
    }

    @Override
    public Page<Transaction> findByUserId(UUID userId, TransactionType type, String category,
                                          OffsetDateTime from, OffsetDateTime to, Pageable pageable) {
        return jpaRepository.findByUserIdFiltered(userId, type, category, from, to, pageable)
                .map(TransactionJpaMapper::toDomain);
    }

    @Override
    public void delete(Transaction transaction) {
        jpaRepository.deleteById(transaction.getId());
    }
}
