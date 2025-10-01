package com.integrixs.backend.messaging.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ infrastructure configuration for internal messaging
 */
@Configuration
@EnableRabbit
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = false)
public class RabbitMQInfrastructureConfig {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RabbitMQInfrastructureConfig.class);

    @Value("${integrix.messaging.queues.inbound.name:integrix.inbound}")
    private String inboundQueue;

    @Value("${integrix.messaging.queues.outbound.name:integrix.outbound}")
    private String outboundQueue;

    @Value("${integrix.messaging.queues.error.name:integrix.error}")
    private String errorQueue;

    @Value("${integrix.messaging.routing.default-exchange:integrix.exchange}")
    private String defaultExchange;

    @Value("${integrix.messaging.routing.topic-exchange:integrix.topic}")
    private String topicExchange;

    @Value("${integrix.messaging.dlq.enabled:true}")
    private boolean dlqEnabled;

    @Value("${integrix.messaging.dlq.ttl:86400000}")
    private long dlqTtl;

    /**
     * Message converter for JSON
     */
    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    /**
     * RabbitMQ admin for dynamic queue/exchange creation
     */
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    /**
     * Configure RabbitMQ template
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setMandatory(true);

        // Configure retry
        RetryTemplate retryTemplate = new RetryTemplate();
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);
        backOffPolicy.setMultiplier(2.0);
        backOffPolicy.setMaxInterval(10000);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        template.setRetryTemplate(retryTemplate);

        // Set default exchange
        template.setExchange(defaultExchange);

        return template;
    }

    /**
     * Configure listener container factory
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setConcurrentConsumers(5);
        factory.setMaxConcurrentConsumers(10);
        factory.setPrefetchCount(10);

        // Configure error handling
        // IntegrixErrorHandler implements RabbitListenerErrorHandler, not ErrorHandler
        // Use a simple error handler for now
        factory.setErrorHandler(t -> {
            logger.error("Error in RabbitMQ listener", t);
        });

        return factory;
    }

    /**
     * Inbound queue
     */
    @Bean
    public Queue inboundQueue() {
        Map<String, Object> args = new HashMap<>();
        if(dlqEnabled) {
            args.put("x-dead-letter-exchange", "");
            args.put("x-dead-letter-routing-key", errorQueue);
        }

        return new Queue(inboundQueue, true, false, false, args);
    }

    /**
     * Outbound queue
     */
    @Bean
    public Queue outboundQueue() {
        Map<String, Object> args = new HashMap<>();
        if(dlqEnabled) {
            args.put("x-dead-letter-exchange", "");
            args.put("x-dead-letter-routing-key", errorQueue);
        }

        return new Queue(outboundQueue, true, false, false, args);
    }

    /**
     * Error/DLQ queue
     */
    @Bean
    public Queue errorQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", dlqTtl);

        return new Queue(errorQueue, true, false, false, args);
    }

    /**
     * Default direct exchange
     */
    @Bean
    public DirectExchange defaultExchange() {
        return new DirectExchange(defaultExchange, true, false);
    }

    /**
     * Topic exchange for routing
     */
    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(topicExchange, true, false);
    }

    /**
     * Bind inbound queue to default exchange
     */
    @Bean
    public Binding inboundBinding() {
        return BindingBuilder
            .bind(inboundQueue())
            .to(defaultExchange())
            .with(inboundQueue);
    }

    /**
     * Bind outbound queue to default exchange
     */
    @Bean
    public Binding outboundBinding() {
        return BindingBuilder
            .bind(outboundQueue())
            .to(defaultExchange())
            .with(outboundQueue);
    }

    /**
     * Bind inbound queue to topic exchange with pattern
     */
    @Bean
    public Binding inboundTopicBinding() {
        return BindingBuilder
            .bind(inboundQueue())
            .to(topicExchange())
            .with("integrix.inbound.*");
    }

    /**
     * Bind outbound queue to topic exchange with pattern
     */
    @Bean
    public Binding outboundTopicBinding() {
        return BindingBuilder
            .bind(outboundQueue())
            .to(topicExchange())
            .with("integrix.outbound.*");
    }
}
