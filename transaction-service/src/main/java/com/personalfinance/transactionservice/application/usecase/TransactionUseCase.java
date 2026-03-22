package com.personalfinance.transactionservice.application.usecase;

import com.personalfinance.transactionservice.application.dto.CreateTransactionCommand;
import com.personalfinance.transactionservice.application.dto.TransactionDto;
import com.personalfinance.transactionservice.application.dto.TransactionFilterCommand;
import com.personalfinance.transactionservice.application.dto.TransactionPageDto;
import com.personalfinance.transactionservice.application.dto.UpdateTransactionCommand;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TransactionUseCase {
    TransactionDto create(UUID userId, CreateTransactionCommand command);
    TransactionDto getById(UUID userId, UUID transactionId);
    TransactionPageDto listByWallet(UUID userId, UUID walletId, Pageable pageable);
    TransactionPageDto listByUser(TransactionFilterCommand filter);
    TransactionDto update(UUID userId, UUID transactionId, UpdateTransactionCommand command);
    void delete(UUID userId, UUID transactionId);
}
