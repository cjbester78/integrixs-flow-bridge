package com.integrixs.data.repository;

import com.integrixs.data.model.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Custom repository implementation for Message entity using JPA Criteria API
 */
@Repository
@RequiredArgsConstructor
public class MessageRepositoryCustomImpl implements MessageRepositoryCustom {
    
    private final EntityManager entityManager;
    
    @Override
    public Double calculateAverageProcessingTimeJpa() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Message> query = cb.createQuery(Message.class);
        Root<Message> message = query.from(Message.class);
        
        // Build predicates
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(message.get("status").in(Message.MessageStatus.PROCESSED, Message.MessageStatus.COMPLETED));
        predicates.add(cb.isNotNull(message.get("receivedAt")));
        predicates.add(cb.isNotNull(message.get("completedAt")));
        
        query.where(predicates.toArray(new Predicate[0]));
        
        List<Message> messages = entityManager.createQuery(query).getResultList();
        
        return calculateAverageProcessingTime(messages);
    }
    
    @Override
    public Double calculateAverageProcessingTimeByFlowIdJpa(UUID flowId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Message> query = cb.createQuery(Message.class);
        Root<Message> message = query.from(Message.class);
        
        // Build predicates
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(message.get("status").in(Message.MessageStatus.PROCESSED, Message.MessageStatus.COMPLETED));
        predicates.add(cb.isNotNull(message.get("receivedAt")));
        predicates.add(cb.isNotNull(message.get("completedAt")));
        predicates.add(cb.equal(message.get("flow").get("id"), flowId));
        
        query.where(predicates.toArray(new Predicate[0]));
        
        List<Message> messages = entityManager.createQuery(query).getResultList();
        
        return calculateAverageProcessingTime(messages);
    }
    
    @Override
    public Double calculateAverageProcessingTimeByBusinessComponentJpa(String businessComponentId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Message> query = cb.createQuery(Message.class);
        Root<Message> message = query.from(Message.class);
        
        // Build predicates
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(message.get("status").in(Message.MessageStatus.PROCESSED, Message.MessageStatus.COMPLETED));
        predicates.add(cb.isNotNull(message.get("receivedAt")));
        predicates.add(cb.isNotNull(message.get("completedAt")));
        predicates.add(cb.equal(message.get("flowExecution").get("businessComponent").get("id"), businessComponentId));
        
        query.where(predicates.toArray(new Predicate[0]));
        
        List<Message> messages = entityManager.createQuery(query).getResultList();
        
        return calculateAverageProcessingTime(messages);
    }
    
    @Override
    public Double calculateAverageProcessingTimeByDateRangeJpa(LocalDateTime startDate, LocalDateTime endDate) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Message> query = cb.createQuery(Message.class);
        Root<Message> message = query.from(Message.class);
        
        // Build predicates
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(message.get("status").in(Message.MessageStatus.PROCESSED, Message.MessageStatus.COMPLETED));
        predicates.add(cb.isNotNull(message.get("receivedAt")));
        predicates.add(cb.isNotNull(message.get("completedAt")));
        predicates.add(cb.between(message.get("receivedAt"), startDate, endDate));
        
        query.where(predicates.toArray(new Predicate[0]));
        
        List<Message> messages = entityManager.createQuery(query).getResultList();
        
        return calculateAverageProcessingTime(messages);
    }
    
    @Override
    public Map<Message.MessageStatus, Long> getStatusCountsWithFilters(Map<String, Object> filters) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<Message> message = query.from(Message.class);
        
        // Build predicates based on filters
        List<Predicate> predicates = new ArrayList<>();
        
        if (filters.containsKey("businessComponentId")) {
            predicates.add(cb.equal(message.get("flowExecution").get("businessComponent").get("id"), 
                                  filters.get("businessComponentId")));
        }
        
        if (filters.containsKey("flowId")) {
            predicates.add(cb.equal(message.get("flow").get("id"), filters.get("flowId")));
        }
        
        if (filters.containsKey("startDate") && filters.containsKey("endDate")) {
            predicates.add(cb.between(message.get("receivedAt"), 
                                    (LocalDateTime) filters.get("startDate"),
                                    (LocalDateTime) filters.get("endDate")));
        }
        
        // Group by status
        query.multiselect(message.get("status"), cb.count(message));
        query.groupBy(message.get("status"));
        
        if (!predicates.isEmpty()) {
            query.where(predicates.toArray(new Predicate[0]));
        }
        
        List<Object[]> results = entityManager.createQuery(query).getResultList();
        
        Map<Message.MessageStatus, Long> statusCounts = new HashMap<>();
        for (Object[] result : results) {
            statusCounts.put((Message.MessageStatus) result[0], (Long) result[1]);
        }
        
        return statusCounts;
    }
    
    /**
     * Calculate average processing time in milliseconds from a list of messages
     */
    private Double calculateAverageProcessingTime(List<Message> messages) {
        if (messages.isEmpty()) {
            return 0.0;
        }
        
        double totalProcessingTime = messages.stream()
            .filter(m -> m.getReceivedAt() != null && m.getCompletedAt() != null)
            .mapToDouble(m -> {
                Duration duration = Duration.between(m.getReceivedAt(), m.getCompletedAt());
                return duration.toMillis();
            })
            .sum();
        
        long countWithTimes = messages.stream()
            .filter(m -> m.getReceivedAt() != null && m.getCompletedAt() != null)
            .count();
        
        return countWithTimes > 0 ? totalProcessingTime / countWithTimes : 0.0;
    }
}