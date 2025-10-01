package com.integrixs.webclient.infrastructure.repository;

import com.integrixs.webclient.domain.model.InboundMessage;
import com.integrixs.webclient.domain.repository.InboundMessageRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In - memory implementation of inbound message repository
 */
@Repository
public class InMemoryInboundMessageRepository implements InboundMessageRepository {

    private final Map<String, InboundMessage> messages = new ConcurrentHashMap<>();

    @Override
    public InboundMessage save(InboundMessage message) {
        if(message.getMessageId() == null) {
            message.setMessageId(UUID.randomUUID().toString());
        }
        messages.put(message.getMessageId(), message);
        return message;
    }

    @Override
    public Optional<InboundMessage> findById(String messageId) {
        return Optional.ofNullable(messages.get(messageId));
    }

    @Override
    public List<InboundMessage> findByStatus(InboundMessage.MessageStatus status) {
        return messages.values().stream()
                .filter(message -> message.getStatus() == status)
                .sorted(Comparator.comparing(InboundMessage::getReceivedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<InboundMessage> findByFlowId(String flowId) {
        return messages.values().stream()
                .filter(message -> flowId.equals(message.getFlowId()))
                .sorted(Comparator.comparing(InboundMessage::getReceivedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<InboundMessage> findByCorrelationId(String correlationId) {
        return messages.values().stream()
                .filter(message -> correlationId.equals(message.getCorrelationId()))
                .sorted(Comparator.comparing(InboundMessage::getReceivedAt))
                .collect(Collectors.toList());
    }

    @Override
    public List<InboundMessage> findByReceivedAtBetween(LocalDateTime start, LocalDateTime end) {
        return messages.values().stream()
                .filter(message -> {
                    LocalDateTime receivedAt = message.getReceivedAt();
                    return receivedAt != null &&
                           (receivedAt.isEqual(start) || receivedAt.isAfter(start)) &&
                           (receivedAt.isEqual(end) || receivedAt.isBefore(end));
                })
                .sorted(Comparator.comparing(InboundMessage::getReceivedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public void updateStatus(String messageId, InboundMessage.MessageStatus status) {
        InboundMessage message = messages.get(messageId);
        if(message != null) {
            message.setStatus(status);
        }
    }

    @Override
    public void deleteById(String messageId) {
        messages.remove(messageId);
    }

    @Override
    public int deleteByReceivedAtBefore(LocalDateTime before) {
        List<String> toDelete = messages.entrySet().stream()
                .filter(entry -> {
                    LocalDateTime receivedAt = entry.getValue().getReceivedAt();
                    return receivedAt != null && receivedAt.isBefore(before);
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        toDelete.forEach(messages::remove);
        return toDelete.size();
    }

    @Override
    public boolean existsDuplicate(InboundMessage message) {
        // Check for duplicates based on correlation ID and payload hash
        if(message.getCorrelationId() != null) {
            return messages.values().stream()
                    .anyMatch(existing ->
                        message.getCorrelationId().equals(existing.getCorrelationId()) &&
                        !message.getMessageId().equals(existing.getMessageId())
                   );
        }

        // Simple duplicate check based on payload and source
        return messages.values().stream()
                .anyMatch(existing -> {
                    if(!message.getMessageId().equals(existing.getMessageId())) {
                        return Objects.equals(message.getPayload(), existing.getPayload()) &&
                               Objects.equals(message.getSource(), existing.getSource()) &&
                               existing.getReceivedAt() != null &&
                               existing.getReceivedAt().isAfter(LocalDateTime.now().minusMinutes(5));
                    }
                    return false;
                });
    }
}
