package com.personalfinance.transactionservice.infrastructure.messaging.publisher;

import com.personalfinance.transactionservice.infrastructure.messaging.event.TransactionCreatedEvent;
import com.personalfinance.transactionservice.infrastructure.messaging.event.TransactionDeletedEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(TransactionEventPublisher.class)
public class NoOpTransactionEventPublisher implements TransactionEventPublisher {

    @Override
    public void publishCreated(TransactionCreatedEvent event) {
        // No-op implementation for tests
    }

    @Override
    public void publishDeleted(TransactionDeletedEvent event) {
        // No-op implementation for tests
    }
}
