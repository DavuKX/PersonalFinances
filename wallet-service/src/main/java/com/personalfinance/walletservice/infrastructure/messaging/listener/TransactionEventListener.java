package com.personalfinance.walletservice.infrastructure.messaging.listener;

import com.personalfinance.walletservice.application.usecase.WalletUseCase;
import com.personalfinance.walletservice.infrastructure.messaging.config.RabbitMQConfig;
import com.personalfinance.walletservice.infrastructure.messaging.event.TransactionCreatedEvent;
import com.personalfinance.walletservice.infrastructure.messaging.event.TransactionDeletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TransactionEventListener {

    private static final Logger logger = LoggerFactory.getLogger(TransactionEventListener.class);
    private final WalletUseCase walletUseCase;

    public TransactionEventListener(WalletUseCase walletUseCase) {
        this.walletUseCase = walletUseCase;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_TRANSACTION_CREATED)
    public void handleTransactionCreated(TransactionCreatedEvent event) {
        logger.info("Processing transaction created event: {} for wallet: {}", event.getTransactionId(), event.getWalletId());
        try {
            BigDecimal balanceChange = calculateBalanceChange(event.getType(), event.getAmount());
            walletUseCase.adjustBalance(event.getWalletId(), event.getUserId(), balanceChange);
            logger.info("Updated wallet balance for transaction: {}", event.getTransactionId());
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
        } catch (Exception e) {
            logger.error("Failed to process transaction deleted event: {}", event.getTransactionId(), e);
        }
    }

    private BigDecimal calculateBalanceChange(String type, BigDecimal amount) {
        return "INCOME".equals(type) ? amount : amount.negate();
    }
}
