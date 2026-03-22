package com.personalfinance.transactionservice.infrastructure.messaging.publisher;

import com.personalfinance.transactionservice.infrastructure.messaging.config.RabbitMQConfig;
import com.personalfinance.transactionservice.infrastructure.messaging.event.TransactionCreatedEvent;
import com.personalfinance.transactionservice.infrastructure.messaging.event.TransactionDeletedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

public class RabbitTransactionEventPublisher implements TransactionEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public RabbitTransactionEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishCreated(TransactionCreatedEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_TRANSACTIONS,
                RabbitMQConfig.ROUTING_KEY_CREATED, event);
    }

    @Override
    public void publishDeleted(TransactionDeletedEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_TRANSACTIONS,
                RabbitMQConfig.ROUTING_KEY_DELETED, event);
    }
}
