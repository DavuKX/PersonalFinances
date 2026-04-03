package com.personalfinance.transactionservice.domain.port;

import com.personalfinance.transactionservice.domain.model.Transaction;
import com.personalfinance.transactionservice.domain.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);
    Page<Transaction> findByWalletIdAndUserId(UUID walletId, UUID userId, Pageable pageable);
    Page<Transaction> findByUserId(UUID userId, TransactionType type, UUID categoryId,
                                   OffsetDateTime from, OffsetDateTime to, Pageable pageable);
    BigDecimal sumSpending(UUID walletId, UUID userId, OffsetDateTime from, OffsetDateTime to);
    void delete(Transaction transaction);
}
