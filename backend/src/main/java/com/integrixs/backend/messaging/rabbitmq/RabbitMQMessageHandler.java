package com.integrixs.backend.messaging.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.backend.service.BackendAdapterExecutor;
import com.integrixs.data.model.CommunicationAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * RabbitMQ message handler implementation for sending and receiving messages.
 * This is not an adapter implementation but a handler for the AdapterExecutionService.
 */
@Component("rabbitmqMessageHandler")
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = false)
public class RabbitMQMessageHandler implements BackendAdapterExecutor.AdapterHandler {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQMessageHandler.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public boolean canHandle(CommunicationAdapter adapter) {
        return adapter != null &&
               adapter.getType() != null &&
               adapter.getType().name().toLowerCase().contains("rabbitmq");
    }

    @Override
    public BackendAdapterExecutor.ExecutionResult execute(
            CommunicationAdapter adapter,
            Map<String, Object> context) {

        try {
            logger.debug("Executing RabbitMQ adapter: {}", adapter.getName());

            // Get adapter configuration
            Map<String, Object> config = new HashMap<>();
            try {
                String configJson = adapter.getConfiguration();
                if(configJson != null && !configJson.isEmpty()) {
                    config = objectMapper.readValue(configJson, Map.class);
                }
            } catch (Exception e) {
                logger.warn("Failed to parse adapter configuration: {}", e.getMessage());
            }

            // Extract RabbitMQ settings
            String exchange = (String) config.getOrDefault("exchange", "");
            String routingKey = (String) config.getOrDefault("routingKey", adapter.getName());
            String queue = (String) config.get("queue");

            // Get message data from context
            Object messageData = context.get("messageData");

            // Handle inbound vs outbound
            if("INBOUND".equals(adapter.getDirection())) {
                return handleInbound(adapter, queue, context);
            } else {
                return handleOutbound(adapter, exchange, routingKey, messageData, context);
            }

        } catch(Exception e) {
            logger.error("Error executing RabbitMQ adapter", e);
            return new BackendAdapterExecutor.ExecutionResult(false, null,
                "RabbitMQ adapter execution failed: " + e.getMessage());
        }
    }

    /**
     * Handle outbound message sending
     */
    private BackendAdapterExecutor.ExecutionResult handleOutbound(
            CommunicationAdapter adapter,
            String exchange,
            String routingKey,
            Object messageData,
            Map<String, Object> context) {

        try {
            // Convert message to JSON
            String jsonMessage = objectMapper.writeValueAsString(messageData);

            // Create message with properties
            MessageProperties properties = new MessageProperties();
            properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            properties.setContentEncoding("UTF-8");
            properties.setMessageId(UUID.randomUUID().toString());

            // Add correlation ID if present
            String correlationId = (String) context.get("correlationId");
            if(correlationId != null) {
                properties.setCorrelationId(correlationId);
            }

            // Add custom headers
            Map<String, Object> headers = (Map<String, Object>) context.get("headers");
            if(headers != null) {
                properties.getHeaders().putAll(headers);
            }

            // Add integration metadata
            properties.setHeader("integrix - adapter - id", adapter.getId().toString());
            properties.setHeader("integrix - adapter - name", adapter.getName());
            properties.setHeader("integrix - timestamp", System.currentTimeMillis());

            // Create message
            Message message = MessageBuilder
                .withBody(jsonMessage.getBytes(StandardCharsets.UTF_8))
                .andProperties(properties)
                .build();

            // Send message
            if(exchange.isEmpty() && routingKey != null) {
                // Send directly to queue
                rabbitTemplate.send(routingKey, message);
                logger.info("Message sent directly to queue: {}", routingKey);
            } else {
                // Send to exchange with routing key
                rabbitTemplate.send(exchange, routingKey, message);
                logger.info("Message sent to exchange: {} with routing key: {}", exchange, routingKey);
            }

            // Prepare result
            Map<String, Object> result = new HashMap<>();
            result.put("messageId", properties.getMessageId());
            result.put("correlationId", properties.getCorrelationId());
            result.put("timestamp", properties.getTimestamp());
            result.put("exchange", exchange);
            result.put("routingKey", routingKey);
            result.put("success", true);

            return new BackendAdapterExecutor.ExecutionResult(true, result);

        } catch(Exception e) {
            logger.error("Failed to send message to RabbitMQ", e);
            return new BackendAdapterExecutor.ExecutionResult(false, null,
                "Failed to send message: " + e.getMessage());
        }
    }

    /**
     * Handle inbound message receiving(typically done via listeners)
     */
    private BackendAdapterExecutor.ExecutionResult handleInbound(
            CommunicationAdapter adapter,
            String queue,
            Map<String, Object> context) {

        try {
            // For inbound, we typically use listeners
            // This method would be called to set up or check the listener

            if(queue == null || queue.isEmpty()) {
                return new BackendAdapterExecutor.ExecutionResult(false, null,
                    "No queue specified for inbound RabbitMQ adapter");
            }

            // Check if queue exists
            try {
                rabbitTemplate.execute(channel -> {
                    channel.queueDeclarePassive(queue);
                    return null;
                });

                Map<String, Object> result = new HashMap<>();
                result.put("queue", queue);
                result.put("status", "listener_configured");
                result.put("message", "Inbound messages will be received via @RabbitListener");

                return new BackendAdapterExecutor.ExecutionResult(true, result);

            } catch(Exception e) {
                return new BackendAdapterExecutor.ExecutionResult(false, null,
                    "Queue does not exist: " + queue + " - " + e.getMessage());
            }

        } catch(Exception e) {
            logger.error("Failed to configure inbound RabbitMQ adapter", e);
            return new BackendAdapterExecutor.ExecutionResult(false, null,
                "Failed to configure inbound: " + e.getMessage());
        }
    }

    /**
     * Get stack trace as string
     */
    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.toString()).append("\n");
        for(StackTraceElement element : e.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
            if(sb.length() > 4000) { // Limit size
                sb.append("\t... truncated");
                break;
            }
        }
        return sb.toString();
    }
}
