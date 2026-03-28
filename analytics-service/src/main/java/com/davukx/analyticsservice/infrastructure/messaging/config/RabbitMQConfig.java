package com.davukx.analyticsservice.infrastructure.messaging.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "spring.rabbitmq.host")
public class RabbitMQConfig {

    public static final String EXCHANGE_TRANSACTIONS = "transaction.exchange";
    public static final String QUEUE_ANALYTICS_CREATED = "analytics.transaction.created";
    public static final String QUEUE_ANALYTICS_DELETED = "analytics.transaction.deleted";
    public static final String ROUTING_KEY_CREATED = "transaction.created";
    public static final String ROUTING_KEY_DELETED = "transaction.deleted";

    @Bean
    public TopicExchange transactionExchange() {
        return new TopicExchange(EXCHANGE_TRANSACTIONS);
    }

    @Bean
    public Queue analyticsCreatedQueue() {
        return QueueBuilder.durable(QUEUE_ANALYTICS_CREATED).build();
    }

    @Bean
    public Queue analyticsDeletedQueue() {
        return QueueBuilder.durable(QUEUE_ANALYTICS_DELETED).build();
    }

    @Bean
    public Binding analyticsCreatedBinding(Queue analyticsCreatedQueue, TopicExchange transactionExchange) {
        return BindingBuilder.bind(analyticsCreatedQueue).to(transactionExchange).with(ROUTING_KEY_CREATED);
    }

    @Bean
    public Binding analyticsDeletedBinding(Queue analyticsDeletedQueue, TopicExchange transactionExchange) {
        return BindingBuilder.bind(analyticsDeletedQueue).to(transactionExchange).with(ROUTING_KEY_DELETED);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}

