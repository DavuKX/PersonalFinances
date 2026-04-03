package com.personalfinance.walletservice.infrastructure.messaging.listener;

import com.personalfinance.walletservice.application.usecase.WalletUseCase;
import com.personalfinance.walletservice.domain.model.BudgetSpending;
import com.personalfinance.walletservice.domain.port.BudgetSpendingRepository;
import com.personalfinance.walletservice.infrastructure.messaging.config.RabbitMQConfig;
import com.personalfinance.walletservice.infrastructure.messaging.event.TransactionCreatedEvent;
import com.personalfinance.walletservice.infrastructure.messaging.event.TransactionDeletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
public class TransactionEventListener {

    private static final Logger logger = LoggerFactory.getLogger(TransactionEventListener.class);
    private final WalletUseCase walletUseCase;
    private final BudgetSpendingRepository budgetSpendingRepository;

    public TransactionEventListener(WalletUseCase walletUseCase,
                                    BudgetSpendingRepository budgetSpendingRepository) {
        this.walletUseCase = walletUseCase;
        this.budgetSpendingRepository = budgetSpendingRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_TRANSACTION_CREATED)
    public void handleTransactionCreated(TransactionCreatedEvent event) {
        logger.info("Processing transaction created event: {} for wallet: {}", event.getTransactionId(), event.getWalletId());
        try {
            BigDecimal balanceChange = calculateBalanceChange(event.getType(), event.getAmount());
            walletUseCase.adjustBalance(event.getWalletId(), event.getUserId(), balanceChange);
            logger.info("Updated wallet balance for transaction: {}", event.getTransactionId());

            if (isBudgetTrackable(event.getType()) && event.getCategoryId() != null) {
                trackSpending(event.getWalletId(), event.getUserId(), event.getCategoryId(), event.getAmount());
            }
        } catch (Exception e) {
            logger.error("Failed to process transaction created event: {}", event.getTransactionId(), e);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_TRANSACTION_DELETED)
    public void handleTransactionDeleted(TransactionDeletedEvent event) {
        logger.info("Processing transaction deleted event: {} for wallet: {}", event.getTransactionId(), event.getWalletId());
        try {
            BigDecimal balanceChange = calculateBalanceChange(event.getType(), event.getAmount()).negate();
            walletUseCase.adjustBalance(event.getWalletId(), event.getUserId(), balanceChange);
            logger.info("Reversed wallet balance for deleted transaction: {}", event.getTransactionId());

            if (isBudgetTrackable(event.getType()) && event.getCategoryId() != null) {
                reverseSpending(event.getWalletId(), event.getUserId(), event.getCategoryId(), event.getAmount());
            }
        } catch (Exception e) {
            logger.error("Failed to process transaction deleted event: {}", event.getTransactionId(), e);
        }
    }

    private void trackSpending(UUID walletId, UUID userId,
                               UUID categoryId, BigDecimal amount) {
        try {
            LocalDate periodStart = LocalDate.now().withDayOfMonth(1);
            LocalDate periodEnd = periodStart.plusMonths(1).minusDays(1);
            BudgetSpending spending = budgetSpendingRepository
                    .findByWalletIdAndCategoryIdAndPeriodStart(walletId, categoryId, periodStart)
                    .orElseGet(() -> BudgetSpending.create(walletId, userId, categoryId, periodStart, periodEnd));
            budgetSpendingRepository.save(spending.addSpending(amount));
        } catch (Exception e) {
            logger.warn("Failed to track budget spending for category {}: {}", categoryId, e.getMessage());
        }
    }

    private void reverseSpending(UUID walletId, UUID userId,
                                 UUID categoryId, BigDecimal amount) {
        try {
            LocalDate periodStart = LocalDate.now().withDayOfMonth(1);
            budgetSpendingRepository
                    .findByWalletIdAndCategoryIdAndPeriodStart(walletId, categoryId, periodStart)
                    .ifPresent(s -> budgetSpendingRepository.save(s.subtractSpending(amount)));
        } catch (Exception e) {
            logger.warn("Failed to reverse budget spending for category {}: {}", categoryId, e.getMessage());
        }
    }

    private BigDecimal calculateBalanceChange(String type, BigDecimal amount) {
        return "INCOME".equals(type) ? amount : amount.negate();
    }

    /** Budgets can be set on EXPENSE and SAVINGS categories. */
    private boolean isBudgetTrackable(String type) {
        return "EXPENSE".equals(type) || "SAVINGS".equals(type);
    }
}





