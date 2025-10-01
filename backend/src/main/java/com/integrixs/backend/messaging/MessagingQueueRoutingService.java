package com.integrixs.backend.messaging;

import com.integrixs.backend.service.ProcessEngineService;
import com.integrixs.backend.service.EnhancedAdapterExecutionService;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.sql.repository.IntegrationFlowSqlRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for routing messages between messaging queues (RabbitMQ, Kafka) and the flow engine
 * Handles inbound messages from queue systems and routes them to appropriate flows
 */
@Service
public class MessagingQueueRoutingService {

    private static final Logger logger = LoggerFactory.getLogger(MessagingQueueRoutingService.class);

    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;

    @Autowired(required = false)
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private EnhancedAdapterExecutionService adapterExecutionService;

    @Autowired
    private ProcessEngineService processEngineService;

    @Autowired
    private IntegrationFlowSqlRepository flowRepository;

    /**
     * RabbitMQ inbound listener
     */
    @RabbitListener(queues = "${integrix.messaging.queues.inbound.name:integrix.inbound}",
                    containerFactory = "rabbitListenerContainerFactory")
    public void handleRabbitMQMessage(IntegrixMessage message,
                                    com.rabbitmq.client.Channel channel,
                                    @Header(name = "amqp_deliveryTag") long deliveryTag) {
        try {
            logger.info("Received RabbitMQ message: {} with correlation ID: {}",
                message.getId(), message.getCorrelationId());

            // Process the message
            processInboundMessage(message, "rabbitmq");

            // Acknowledge the message
            channel.basicAck(deliveryTag, false);

        } catch(Exception e) {
            logger.error("Error processing RabbitMQ message", e);
            try {
                // Reject and don't requeue(will go to DLQ if configured)
                channel.basicNack(deliveryTag, false, false);
            } catch(Exception ackError) {
                logger.error("Error acknowledging message", ackError);
            }
        }
    }

    /**
     * Kafka inbound listener
     */
    @KafkaListener(topics = "${integrix.messaging.topics.inbound:integrix - inbound}",
                   containerFactory = "kafkaListenerContainerFactory")
    public void handleKafkaMessage(@Payload IntegrixMessage message,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                 @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                 @Header(KafkaHeaders.OFFSET) long offset,
                                 Acknowledgment acknowledgment) {
        try {
            logger.info("Received Kafka message: {} from topic: {} partition: {} offset: {}",
                message.getId(), topic, partition, offset);

            // Process the message
            processInboundMessage(message, "kafka");

            // Acknowledge the message
            acknowledgment.acknowledge();

        } catch(Exception e) {
            logger.error("Error processing Kafka message", e);
            // Don't acknowledge - message will be redelivered
        }
    }

    /**
     * Process inbound message from any source
     */
    private void processInboundMessage(IntegrixMessage message, String source) {
        try {
            // Extract flow ID
            String flowId = message.getFlowId();
            if(flowId == null) {
                logger.error("No flow ID in message: {}", message.getId());
                sendToErrorQueue(message, "No flow ID specified", source);
                return;
            }

            // Load the integration flow
            IntegrationFlow flow;
            try {
                flow = flowRepository.findById(UUID.fromString(flowId))
                    .orElseThrow(() -> new IllegalArgumentException("Flow not found: " + flowId));
            } catch(Exception e) {
                logger.error("Failed to load flow: {}", flowId, e);
                sendToErrorQueue(message, "Failed to load flow: " + e.getMessage(), source);
                return;
            }

            // Prepare execution context
            Map<String, Object> context = new HashMap<>();
            context.put("messageId", message.getId());
            context.put("correlationId", message.getCorrelationId());
            context.put("source", source);
            context.put("messageType", message.getMessageType());
            context.putAll(message.getMetadata());

            // For now, execute directly via flow engine
            // TODO: Add Camunda integration based on flow type or other criteria
            executeViaFlowEngine(flow, message, context);

        } catch(Exception e) {
            logger.error("Error processing inbound message", e);
            sendToErrorQueue(message, e.getMessage(), source);
        }
    }

    /**
     * Execute flow via Camunda process engine
     */
    private void executeViaCamunda(IntegrationFlow flow, IntegrixMessage message, Map<String, Object> context) {
        try {
            // Get process definition ID
            // TODO: Store process definition ID in a more appropriate location
            String processDefinitionId = null;

            if(processDefinitionId == null) {
                // Deploy process if not already deployed
                var deployResult = processEngineService.deployProcess(flow);
                if(!deployResult.isSuccess()) {
                    throw new RuntimeException("Failed to deploy process: " + deployResult.getError());
                }
                processDefinitionId = deployResult.getProcessDefinition().getId();
            }

            // Prepare process variables
            Map<String, Object> variables = new HashMap<>(context);
            variables.put("integrixMessage", message);
            variables.put("messagePayload", message.getPayload());
            variables.put("flowId", flow.getId().toString());

            // Start process instance
            var startResult = processEngineService.startProcess(processDefinitionId, variables);

            if(startResult.isSuccess()) {
                logger.info("Started Camunda process instance: {} for message: {}",
                    startResult.getProcessInstance().getId(), message.getId());
            } else {
                throw new RuntimeException("Failed to start process: " + startResult.getError());
            }

        } catch(Exception e) {
            logger.error("Error executing via Camunda", e);
            throw new RuntimeException("Camunda execution failed", e);
        }
    }

    /**
     * Execute flow via flow engine
     */
    private void executeViaFlowEngine(IntegrationFlow flow, IntegrixMessage message, Map<String, Object> context) {
        try {
            // Execute the flow
            String correlationId = message.getCorrelationId() != null ? message.getCorrelationId() : message.getId();
            String payload = message.getPayload() != null ? message.getPayload().toString() : "";
            var resultFuture = adapterExecutionService.executeFlow(flow.getId().toString(), correlationId, payload);

            // Wait for the result
            var result = resultFuture.get();

            if(result.isSuccess()) {
                logger.info("Flow executed successfully for message: {}", message.getId());

                // TODO: Determine whether to send to outbound based on flow type or adapter configuration
                // For now, always send result to outbound
                sendToOutbound(message, result.getData(), context);
            } else {
                throw new RuntimeException("Flow execution failed: " + result.getErrorMessage());
            }

        } catch(Exception e) {
            logger.error("Error executing via flow engine", e);
            throw new RuntimeException("Flow engine execution failed", e);
        }
    }

    /**
     * Send message to outbound queue/topic
     */
    public void sendToOutbound(IntegrixMessage originalMessage, Object resultData, Map<String, Object> context) {
        try {
            // Create outbound message
            IntegrixMessage outboundMessage = IntegrixMessage.builder()
                .withCorrelationId(originalMessage.getCorrelationId())
                .withFlowId(originalMessage.getFlowId())
                .withMessageType("response")
                .withPayload(resultData)
                .withSource("integrix")
                .withTarget(originalMessage.getSource())
                .withMetadata(context)
                .build();

            // Determine target system
            String targetSystem = (String) context.get("targetSystem");
            if("kafka".equals(targetSystem) && kafkaTemplate != null) {
                // Send to Kafka
                String topic = (String) context.getOrDefault("targetTopic",
                    "${integrix.messaging.topics.outbound:integrix - outbound}");
                kafkaTemplate.send(topic, outboundMessage.getCorrelationId(), outboundMessage);
                logger.info("Sent outbound message to Kafka topic: {}", topic);
            } else if(rabbitTemplate != null) {
                // Send to RabbitMQ
                String exchange = (String) context.getOrDefault("targetExchange", "");
                String routingKey = (String) context.getOrDefault("targetRoutingKey",
                    "${integrix.messaging.queues.outbound.name:integrix.outbound}");
                rabbitTemplate.convertAndSend(exchange, routingKey, outboundMessage);
                logger.info("Sent outbound message to RabbitMQ routing key: {}", routingKey);
            } else {
                logger.warn("No messaging system available for outbound message");
            }

        } catch(Exception e) {
            logger.error("Error sending outbound message", e);
        }
    }

    /**
     * Send message to error queue/topic
     */
    private void sendToErrorQueue(IntegrixMessage message, String error, String source) {
        try {
            // Add error info
            message.getMetadata().put("error", error);
            message.getMetadata().put("errorTimestamp", System.currentTimeMillis());
            message.getMetadata().put("originalSource", source);

            if("kafka".equals(source) && kafkaTemplate != null) {
                // Send to Kafka error topic
                kafkaTemplate.send("${integrix.messaging.topics.error:integrix - error}",
                    message.getCorrelationId(), message);
                logger.info("Sent error message to Kafka error topic");
            } else if(rabbitTemplate != null) {
                // Send to RabbitMQ error queue
                rabbitTemplate.convertAndSend("${integrix.messaging.queues.error.name:integrix.error}", message);
                logger.info("Sent error message to RabbitMQ error queue");
            }

        } catch(Exception e) {
            logger.error("Failed to send message to error queue", e);
        }
    }
}
