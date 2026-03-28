package com.davukx.analyticsservice.infrastructure.messaging.consumer;

import com.davukx.analyticsservice.application.service.AnalyticsApplicationService;
import com.davukx.analyticsservice.infrastructure.messaging.config.RabbitMQConfig;
import com.davukx.analyticsservice.infrastructure.messaging.event.TransactionCreatedEvent;
import com.davukx.analyticsservice.infrastructure.messaging.event.TransactionDeletedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "spring.rabbitmq.host")
public class TransactionEventConsumer {

    private final AnalyticsApplicationService analyticsService;

    public TransactionEventConsumer(AnalyticsApplicationService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ANALYTICS_CREATED)
    public void handleTransactionCreated(TransactionCreatedEvent event) {
        analyticsService.processTransactionCreated(event);
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ANALYTICS_DELETED)
    public void handleTransactionDeleted(TransactionDeletedEvent event) {
        analyticsService.processTransactionDeleted(event);
    }
}

