package com.integrixs.backend.messaging.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.stereotype.Component;

/**
 * Custom error handler for RabbitMQ listeners
 */
@Component("integrixRabbitErrorHandler")
public class IntegrixErrorHandler implements RabbitListenerErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(IntegrixErrorHandler.class);

    @Override
    public Object handleError(Message amqpMessage, com.rabbitmq.client.Channel channel,
                            org.springframework.messaging.Message<?> message,
                            ListenerExecutionFailedException exception) throws Exception {
        // Call the main error handling logic
        return handleError(amqpMessage, message, exception);
    }

    // This is the main error handling method
    public Object handleError(Message amqpMessage, org.springframework.messaging.Message<?> message,
                            ListenerExecutionFailedException exception) throws Exception {

        logger.error("Error processing message: {}", exception.getMessage(), exception);

        // Extract message details
        String messageId = amqpMessage.getMessageProperties().getMessageId();
        String correlationId = amqpMessage.getMessageProperties().getCorrelationId();
        String routingKey = amqpMessage.getMessageProperties().getReceivedRoutingKey();

        logger.error("Failed message details - ID: {}, CorrelationId: {}, RoutingKey: {}",
            messageId, correlationId, routingKey);

        // Check if we should requeue
        Integer retryCount = (Integer) amqpMessage.getMessageProperties()
            .getHeaders().get("x - retry - count");

        if(retryCount == null) {
            retryCount = 0;
        }

        if(retryCount < 3) {
            // Add retry count and requeue
            amqpMessage.getMessageProperties().setHeader("x - retry - count", retryCount + 1);
            throw new MessageProcessingException("Message processing failed, will retry", exception);
        } else {
            // Max retries reached, send to DLQ
            logger.error("Max retries reached for message: {}", messageId);
            throw new MessageProcessingException("Message processing failed after max retries", exception, false);
        }
    }

    /**
     * Custom exception for message processing
     */
    public static class MessageProcessingException extends RuntimeException {
        private final boolean shouldRequeue;

        public MessageProcessingException(String message, Throwable cause) {
            this(message, cause, true);
        }

        public MessageProcessingException(String message, Throwable cause, boolean shouldRequeue) {
            super(message, cause);
            this.shouldRequeue = shouldRequeue;
        }

        public boolean shouldRequeue() {
            return shouldRequeue;
        }
    }
}
