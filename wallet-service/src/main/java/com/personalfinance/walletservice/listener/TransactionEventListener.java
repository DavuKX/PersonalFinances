package com.personalfinance.walletservice.listener;

import com.personalfinance.walletservice.config.RabbitMQConfig;
import org.springframework.stereotype.Component;

/**
 * Listener for transaction events from RabbitMQ.
 * Will be activated when the Transaction Service is implemented.
 *
 * Example usage:
 *
 * @RabbitListener(queues = RabbitMQConfig.QUEUE_TRANSACTION_CREATED)
 * public void handleTransactionCreated(TransactionEvent event) {
 *     // Update wallet balance: subtract for expenses, add for income
 * }
 *
 * @RabbitListener(queues = RabbitMQConfig.QUEUE_TRANSACTION_DELETED)
 * public void handleTransactionDeleted(TransactionEvent event) {
 *     // Reverse the balance change
 * }
 */
@Component
public class TransactionEventListener {

    // TODO: Implement when Transaction Service is ready
    // This listener will consume TransactionCreated and TransactionDeleted events
    // to update wallet balances accordingly.
}
