package com.personalfinance.transactionservice.infrastructure.messaging.config;

import com.personalfinance.transactionservice.infrastructure.messaging.event.TransactionCreatedEvent;
import com.personalfinance.transactionservice.infrastructure.messaging.event.TransactionDeletedEvent;
import com.personalfinance.transactionservice.infrastructure.messaging.publisher.TransactionEventPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingFallbackConfig {

    @Bean
    @ConditionalOnMissingBean(TransactionEventPublisher.class)
    public TransactionEventPublisher noOpTransactionEventPublisher() {
        return new TransactionEventPublisher() {
            @Override
            public void publishCreated(TransactionCreatedEvent event) {}

            @Override
            public void publishDeleted(TransactionDeletedEvent event) {}
        };
    }
}
