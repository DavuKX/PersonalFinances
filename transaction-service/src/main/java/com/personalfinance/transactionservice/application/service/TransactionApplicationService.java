package com.personalfinance.transactionservice.application.service;

import com.personalfinance.transactionservice.application.dto.CreateTransactionCommand;
import com.personalfinance.transactionservice.application.dto.TransactionDto;
import com.personalfinance.transactionservice.application.dto.TransactionFilterCommand;
import com.personalfinance.transactionservice.application.dto.TransactionPageDto;
import com.personalfinance.transactionservice.application.dto.UpdateTransactionCommand;
import com.personalfinance.transactionservice.application.usecase.TransactionUseCase;
import com.personalfinance.transactionservice.domain.exception.TransactionNotFoundException;
import com.personalfinance.transactionservice.domain.model.Transaction;
import com.personalfinance.transactionservice.domain.port.TransactionRepository;
import com.personalfinance.transactionservice.infrastructure.messaging.event.TransactionCreatedEvent;
import com.personalfinance.transactionservice.infrastructure.messaging.event.TransactionDeletedEvent;
import com.personalfinance.transactionservice.infrastructure.messaging.publisher.TransactionEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TransactionApplicationService implements TransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final TransactionEventPublisher eventPublisher;

    public TransactionApplicationService(TransactionRepository transactionRepository,
                                         TransactionEventPublisher eventPublisher) {
        this.transactionRepository = transactionRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public TransactionDto create(UUID userId, CreateTransactionCommand command) {
        Transaction transaction = Transaction.create(userId, command.walletId(), command.type(),
                command.amount(), command.currency(), command.category(), command.subCategory(),
                command.description(), command.transactionDate());
        Transaction saved = transactionRepository.save(transaction);
        eventPublisher.publishCreated(new TransactionCreatedEvent(
                saved.getId(), saved.getUserId(), saved.getWalletId(),
                saved.getType(), saved.getAmount(), saved.getCurrency()));
        return toDto(saved);
    }

    @Override
    public TransactionDto getById(UUID userId, UUID transactionId) {
        return toDto(loadTransaction(userId, transactionId));
    }

    @Override
    public TransactionPageDto listByWallet(UUID userId, UUID walletId, Pageable pageable) {
        return toPageDto(transactionRepository.findByWalletIdAndUserId(walletId, userId, pageable));
    }

    @Override
    public TransactionPageDto listByUser(TransactionFilterCommand filter) {
        return toPageDto(transactionRepository.findByUserId(
                filter.userId(), filter.type(), filter.category(), filter.from(), filter.to(), filter.pageable()));
    }

    @Override
    @Transactional
    public TransactionDto update(UUID userId, UUID transactionId, UpdateTransactionCommand command) {
        Transaction transaction = loadTransaction(userId, transactionId);
        Transaction updated = transaction.withDetails(command.type(), command.amount(), command.category(),
                command.subCategory(), command.description(), command.transactionDate());
        return toDto(transactionRepository.save(updated));
    }

    @Override
    @Transactional
    public void delete(UUID userId, UUID transactionId) {
        Transaction transaction = loadTransaction(userId, transactionId);
        transactionRepository.delete(transaction);
        eventPublisher.publishDeleted(new TransactionDeletedEvent(
                transaction.getId(), transaction.getUserId(), transaction.getWalletId(),
                transaction.getType(), transaction.getAmount(), transaction.getCurrency()));
    }

    private Transaction loadTransaction(UUID userId, UUID transactionId) {
        return transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));
    }

    private TransactionPageDto toPageDto(Page<Transaction> page) {
        return new TransactionPageDto(
                page.getContent().stream().map(this::toDto).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    private TransactionDto toDto(Transaction t) {
        return new TransactionDto(t.getId(), t.getUserId(), t.getWalletId(), t.getType(), t.getAmount(),
                t.getCurrency(), t.getCategory(), t.getSubCategory(), t.getDescription(),
                t.getTransactionDate(), t.getCreatedAt(), t.getUpdatedAt());
    }
}
