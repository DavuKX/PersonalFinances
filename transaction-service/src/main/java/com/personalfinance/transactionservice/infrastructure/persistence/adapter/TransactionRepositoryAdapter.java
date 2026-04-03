package com.personalfinance.transactionservice.infrastructure.persistence.adapter;

import com.personalfinance.transactionservice.domain.model.Transaction;
import com.personalfinance.transactionservice.domain.model.TransactionType;
import com.personalfinance.transactionservice.domain.port.TransactionRepository;
import com.personalfinance.transactionservice.infrastructure.persistence.entity.TransactionJpaEntity;
import com.personalfinance.transactionservice.infrastructure.persistence.mapper.TransactionJpaMapper;
import com.personalfinance.transactionservice.infrastructure.persistence.repository.TransactionJpaRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
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
    public Page<Transaction> findByUserId(UUID userId, TransactionType type, UUID categoryId,
                                          OffsetDateTime from, OffsetDateTime to, Pageable pageable) {
        Specification<TransactionJpaEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("userId"), userId));
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("categoryId"), categoryId));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), to));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return jpaRepository.findAll(spec, pageable).map(TransactionJpaMapper::toDomain);
    }

    @Override
    public void delete(Transaction transaction) {
        jpaRepository.deleteById(transaction.getId());
    }

    @Override
    public java.math.BigDecimal sumSpending(UUID walletId, UUID userId, OffsetDateTime from, OffsetDateTime to) {
        return jpaRepository.sumAmountByWalletIdAndUserIdAndTypeInAndDateBetween(
                walletId, userId,
                List.of(TransactionType.EXPENSE, TransactionType.SAVINGS),
                from, to);
    }
}
