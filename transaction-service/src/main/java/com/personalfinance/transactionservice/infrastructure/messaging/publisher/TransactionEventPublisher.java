package com.personalfinance.transactionservice.infrastructure.messaging.publisher;

import com.personalfinance.transactionservice.infrastructure.messaging.event.TransactionCreatedEvent;
import com.personalfinance.transactionservice.infrastructure.messaging.event.TransactionDeletedEvent;

public interface TransactionEventPublisher {
    void publishCreated(TransactionCreatedEvent event);
    void publishDeleted(TransactionDeletedEvent event);
}
