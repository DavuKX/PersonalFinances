package com.personalfinance.walletservice.infrastructure.messaging.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "spring.rabbitmq.host")
public class RabbitMQConfig {

    public static final String EXCHANGE_TRANSACTIONS = "transaction.exchange";
    public static final String QUEUE_TRANSACTION_CREATED = "wallet.transaction.created";
    public static final String QUEUE_TRANSACTION_DELETED = "wallet.transaction.deleted";
    public static final String ROUTING_KEY_CREATED = "transaction.created";
    public static final String ROUTING_KEY_DELETED = "transaction.deleted";

    @Bean
    public TopicExchange transactionExchange() {
        return new TopicExchange(EXCHANGE_TRANSACTIONS);
    }

    @Bean
    public Queue transactionCreatedQueue() {
        return QueueBuilder.durable(QUEUE_TRANSACTION_CREATED).build();
    }

    @Bean
    public Queue transactionDeletedQueue() {
        return QueueBuilder.durable(QUEUE_TRANSACTION_DELETED).build();
    }

    @Bean
    public Binding bindingCreated(Queue transactionCreatedQueue, TopicExchange transactionExchange) {
        return BindingBuilder.bind(transactionCreatedQueue).to(transactionExchange).with(ROUTING_KEY_CREATED);
    }

    @Bean
    public Binding bindingDeleted(Queue transactionDeletedQueue, TopicExchange transactionExchange) {
        return BindingBuilder.bind(transactionDeletedQueue).to(transactionExchange).with(ROUTING_KEY_DELETED);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
