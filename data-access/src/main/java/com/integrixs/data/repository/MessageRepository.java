package com.integrixs.data.repository;

import com.integrixs.data.model.Message;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.model.FlowExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID>, MessageRepositoryCustom {

    Optional<Message> findByMessageId(String messageId);

    Page<Message> findByFlow(IntegrationFlow flow, Pageable pageable);

    Page<Message> findByFlowExecution(FlowExecution flowExecution, Pageable pageable);

    List<Message> findByStatus(Message.MessageStatus status);

    @Query("SELECT m FROM Message m WHERE m.status = :status ORDER BY m.priority DESC, m.receivedAt ASC")
    List<Message> findByStatusOrderByPriorityAndReceivedAt(@Param("status") Message.MessageStatus status);

    @Query("SELECT m FROM Message m WHERE m.status IN :statuses AND m.retryCount < :maxRetries")
    List<Message> findRetryableMessages(@Param("statuses") List<Message.MessageStatus> statuses,
                                       @Param("maxRetries") Integer maxRetries);

    List<Message> findByCorrelationIdOrderByReceivedAt(String correlationId);

    Page<Message> findByReceivedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Long countByFlowIdAndStatus(UUID flowId, Message.MessageStatus status);

    @Query("SELECT m FROM Message m WHERE m.status IN :statuses ORDER BY m.priority DESC, m.createdAt ASC")
    List<Message> findByStatusInOrderByPriorityDescCreatedAtAsc(@Param("statuses") List<Message.MessageStatus> statuses);

    @Modifying
    @Query("UPDATE Message m SET m.messageContent = :content WHERE m.id = :id")
    void updateMessageContent(@Param("id") UUID id, @Param("content") String content);

    @Query("SELECT m.messageContent FROM Message m WHERE m.id = :id")
    String findMessageContentById(@Param("id") UUID id);

    // Statistics queries
    @Query("SELECT COUNT(m) FROM Message m WHERE m.status = :status")
    Long countByStatus(@Param("status") Message.MessageStatus status);

    @Query("SELECT m.status, COUNT(m) FROM Message m GROUP BY m.status")
    List<Object[]> countByStatusGrouped();

    @Query(value = "SELECT AVG(EXTRACT(EPOCH FROM(m.completed_at - m.received_at)) * 1000) FROM message m " +
           "WHERE m.status IN('PROCESSED', 'COMPLETED') " +
           "AND m.received_at IS NOT NULL AND m.completed_at IS NOT NULL", nativeQuery = true)
    Double calculateAverageProcessingTime();

    @Query(value = "SELECT AVG(EXTRACT(EPOCH FROM(m.completed_at - m.received_at)) * 1000) FROM message m " +
           "WHERE m.status IN('PROCESSED', 'COMPLETED') " +
           "AND m.flow_id = :flowId " +
           "AND m.received_at IS NOT NULL AND m.completed_at IS NOT NULL", nativeQuery = true)
    Double calculateAverageProcessingTimeByFlowId(@Param("flowId") UUID flowId);

    @Query(value = "SELECT AVG(EXTRACT(EPOCH FROM(m.completed_at - m.received_at)) * 1000) FROM message m " +
           "WHERE m.status IN('PROCESSED', 'COMPLETED') " +
           "AND m.received_at BETWEEN :startDate AND :endDate " +
           "AND m.received_at IS NOT NULL AND m.completed_at IS NOT NULL", nativeQuery = true)
    Double calculateAverageProcessingTimeForPeriod(@Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    // Additional filtered statistics queries
    @Query("SELECT m.status, COUNT(m) FROM Message m " +
           "WHERE m.flowExecution.businessComponent.id = :businessComponentId " +
           "GROUP BY m.status")
    List<Object[]> countByStatusGroupedAndBusinessComponent(@Param("businessComponentId") String businessComponentId);

    @Query("SELECT m.status, COUNT(m) FROM Message m " +
           "WHERE m.flow.id = :flowId " +
           "GROUP BY m.status")
    List<Object[]> countByStatusGroupedAndFlow(@Param("flowId") UUID flowId);

    @Query("SELECT m.status, COUNT(m) FROM Message m " +
           "WHERE m.receivedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY m.status")
    List<Object[]> countByStatusGroupedAndDateRange(@Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT AVG(EXTRACT(EPOCH FROM(m.completed_at - m.received_at)) * 1000) FROM message m " +
           "JOIN flow_execution fe ON m.flow_execution_id = fe.id " +
           "WHERE m.status IN('PROCESSED', 'COMPLETED') " +
           "AND fe.business_component_id = :businessComponentId " +
           "AND m.received_at IS NOT NULL AND m.completed_at IS NOT NULL", nativeQuery = true)
    Double calculateAverageProcessingTimeByBusinessComponent(@Param("businessComponentId") String businessComponentId);

    @Query(value = "SELECT AVG(EXTRACT(EPOCH FROM(m.completed_at - m.received_at)) * 1000) FROM message m " +
           "WHERE m.status IN('PROCESSED', 'COMPLETED') " +
           "AND m.received_at BETWEEN :startDate AND :endDate " +
           "AND m.received_at IS NOT NULL AND m.completed_at IS NOT NULL", nativeQuery = true)
    Double calculateAverageProcessingTimeByDateRange(@Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);
}
