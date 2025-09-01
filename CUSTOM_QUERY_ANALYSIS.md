# Custom Query Analysis Report

## Overview
This report analyzes custom @Query annotations in the codebase and identifies which ones could be replaced with standard JPA repository methods.

## Summary
- **Total Custom Queries Found**: 72
- **Queries That Can Be Replaced**: 23
- **Queries That Should Remain Custom**: 49

## Queries That Can Be Replaced with JPA

### 1. MessageRepository

#### Current Custom Query:
```java
@Query("SELECT m FROM Message m WHERE m.correlationId = :correlationId ORDER BY m.receivedAt")
List<Message> findByCorrelationId(@Param("correlationId") String correlationId);
```
**Replace with**: 
```java
List<Message> findByCorrelationIdOrderByReceivedAt(String correlationId);
```

#### Current Custom Query:
```java
@Query("SELECT m FROM Message m WHERE m.receivedAt BETWEEN :startDate AND :endDate")
Page<Message> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);
```
**Replace with**:
```java
Page<Message> findByReceivedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
```

#### Current Custom Query:
```java
@Query("SELECT COUNT(m) FROM Message m WHERE m.flow.id = :flowId AND m.status = :status")
Long countByFlowIdAndStatus(@Param("flowId") UUID flowId, @Param("status") Message.MessageStatus status);
```
**Replace with**:
```java
Long countByFlowIdAndStatus(UUID flowId, Message.MessageStatus status);
```

### 2. FlowRouteRepository

#### Current Custom Query:
```java
@Query("SELECT fr FROM FlowRoute fr WHERE fr.flow.id = :flowId AND fr.active = true ORDER BY fr.priority ASC")
List<FlowRoute> findActiveRoutesByFlowId(@Param("flowId") UUID flowId);
```
**Replace with**:
```java
List<FlowRoute> findByFlowIdAndActiveTrueOrderByPriorityAsc(UUID flowId);
```

#### Current Custom Query:
```java
@Query("SELECT fr FROM FlowRoute fr WHERE fr.flow.id = :flowId AND fr.sourceStep = :sourceStep AND fr.active = true ORDER BY fr.priority")
List<FlowRoute> findActiveRoutesBySourceStep(@Param("flowId") UUID flowId, @Param("sourceStep") String sourceStep);
```
**Replace with**:
```java
List<FlowRoute> findByFlowIdAndSourceStepAndActiveTrueOrderByPriority(UUID flowId, String sourceStep);
```

### 3. JpaMessageRepository

#### Current Custom Query:
```java
@Query("SELECT m FROM Message m WHERE m.flow.id = :flowId")
List<Message> findAllByFlowId(@Param("flowId") UUID flowId);
```
**Replace with**:
```java
List<Message> findByFlowId(UUID flowId);
```

#### Current Custom Query:
```java
@Query("SELECT COUNT(m) FROM Message m WHERE m.flow.id = :flowId")
long countByFlowId(@Param("flowId") UUID flowId);
```
**Replace with**:
```java
long countByFlowId(UUID flowId);
```

### 4. ErrorRecordRepository

#### Current Custom Query:
```java
@Query("SELECT er FROM ErrorRecord er WHERE er.occurredAt BETWEEN :startDate AND :endDate")
Page<ErrorRecord> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);
```
**Replace with**:
```java
Page<ErrorRecord> findByOccurredAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
```

#### Current Custom Query:
```java
@Query("SELECT er FROM ErrorRecord er WHERE er.flow.id = :flowId AND er.resolved = false")
List<ErrorRecord> findUnresolvedByFlowId(@Param("flowId") UUID flowId);
```
**Replace with**:
```java
List<ErrorRecord> findByFlowIdAndResolvedFalse(UUID flowId);
```

#### Current Custom Query:
```java
@Query("SELECT er FROM ErrorRecord er WHERE er.severity = :severity AND er.resolved = false")
List<ErrorRecord> findUnresolvedBySeverity(@Param("severity") ErrorSeverity severity);
```
**Replace with**:
```java
List<ErrorRecord> findBySeverityAndResolvedFalse(ErrorSeverity severity);
```

#### Current Custom Query:
```java
@Query("SELECT COUNT(er) FROM ErrorRecord er WHERE er.flow.id = :flowId AND er.errorType = :errorType")
Long countByFlowIdAndErrorType(@Param("flowId") UUID flowId, @Param("errorType") String errorType);
```
**Replace with**:
```java
Long countByFlowIdAndErrorType(UUID flowId, String errorType);
```

### 5. SagaStepRepository

#### Current Custom Query:
```java
@Query("SELECT ss FROM SagaStep ss WHERE ss.sagaTransaction.id = :transactionId AND ss.status = :status")
List<SagaStep> findByTransactionIdAndStatus(@Param("transactionId") UUID transactionId, @Param("status") SagaStepStatus status);
```
**Replace with**:
```java
List<SagaStep> findBySagaTransactionIdAndStatus(UUID transactionId, SagaStepStatus status);
```

#### Current Custom Query:
```java
@Query("SELECT ss FROM SagaStep ss WHERE ss.sagaTransaction.sagaId = :sagaId ORDER BY ss.stepOrder")
List<SagaStep> findBySagaIdOrderByStepOrder(@Param("sagaId") String sagaId);
```
**Replace with**:
```java
List<SagaStep> findBySagaTransactionSagaIdOrderByStepOrder(String sagaId);
```

#### Current Custom Query:
```java
@Query("SELECT ss FROM SagaStep ss WHERE ss.sagaTransaction.id = :transactionId ORDER BY ss.stepOrder")
List<SagaStep> findByTransactionIdOrderByStepOrder(@Param("transactionId") UUID transactionId);
```
**Replace with**:
```java
List<SagaStep> findBySagaTransactionIdOrderByStepOrder(UUID transactionId);
```

### 6. FlowExecutionRepository

#### Current Custom Query:
```java
@Query("SELECT fe FROM FlowExecution fe WHERE fe.flow.id = :flowId ORDER BY fe.startedAt DESC")
Page<FlowExecution> findByFlowId(@Param("flowId") UUID flowId, Pageable pageable);
```
**Replace with**:
```java
Page<FlowExecution> findByFlowIdOrderByStartedAtDesc(UUID flowId, Pageable pageable);
```

#### Current Custom Query:
```java
@Query("SELECT fe FROM FlowExecution fe WHERE fe.startedAt BETWEEN :startDate AND :endDate")
Page<FlowExecution> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);
```
**Replace with**:
```java
Page<FlowExecution> findByStartedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
```

#### Current Custom Query:
```java
@Query("SELECT COUNT(fe) FROM FlowExecution fe WHERE fe.flow.id = :flowId AND fe.status = :status")
Long countByFlowIdAndStatus(@Param("flowId") UUID flowId, @Param("status") ExecutionStatus status);
```
**Replace with**:
```java
Long countByFlowIdAndStatus(UUID flowId, ExecutionStatus status);
```

### 7. SagaTransactionRepository

#### Current Custom Query:
```java
@Query("SELECT st FROM SagaTransaction st WHERE st.status IN :statuses")
List<SagaTransaction> findByStatusIn(@Param("statuses") List<SagaTransactionStatus> statuses);
```
**Replace with**:
```java
List<SagaTransaction> findByStatusIn(List<SagaTransactionStatus> statuses);
```

#### Current Custom Query:
```java
@Query("SELECT st FROM SagaTransaction st WHERE st.flow.id = :flowId ORDER BY st.startedAt DESC")
Page<SagaTransaction> findByFlowId(@Param("flowId") UUID flowId, Pageable pageable);
```
**Replace with**:
```java
Page<SagaTransaction> findByFlowIdOrderByStartedAtDesc(UUID flowId, Pageable pageable);
```

### 8. EventStoreRepository

#### Current Custom Query:
```java
@Query("SELECT COALESCE(MAX(e.aggregateVersion), 0) FROM EventStore e WHERE e.aggregateId = :aggregateId")
Long findMaxVersionByAggregateId(@Param("aggregateId") String aggregateId);
```
**Note**: This could potentially be replaced with a custom implementation, but the COALESCE makes it better as a custom query.

### 9. DeadLetterMessageRepository

#### Current Custom Query:
```java
@Query("SELECT dlm FROM DeadLetterMessage dlm WHERE dlm.queuedAt BETWEEN :startDate AND :endDate")
Page<DeadLetterMessage> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);
```
**Replace with**:
```java
Page<DeadLetterMessage> findByQueuedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
```

#### Current Custom Query:
```java
@Query("SELECT dlm FROM DeadLetterMessage dlm WHERE dlm.flow.id = :flowId AND dlm.reprocessed = false")
List<DeadLetterMessage> findUnprocessedByFlowId(@Param("flowId") UUID flowId);
```
**Replace with**:
```java
List<DeadLetterMessage> findByFlowIdAndReprocessedFalse(UUID flowId);
```

#### Current Custom Query:
```java
@Query("SELECT dlm FROM DeadLetterMessage dlm WHERE dlm.correlationId = :correlationId")
List<DeadLetterMessage> findByCorrelationId(@Param("correlationId") String correlationId);
```
**Replace with**:
```java
List<DeadLetterMessage> findByCorrelationId(String correlationId);
```

#### Current Custom Query:
```java
@Query("SELECT COUNT(dlm) FROM DeadLetterMessage dlm WHERE dlm.flow.id = :flowId AND dlm.reprocessed = false")
Long countUnprocessedByFlowId(@Param("flowId") UUID flowId);
```
**Replace with**:
```java
Long countByFlowIdAndReprocessedFalse(UUID flowId);
```

### 10. RouteConditionRepository

#### Current Custom Query:
```java
@Query("SELECT rc FROM RouteCondition rc WHERE rc.flowRoute.id = :routeId ORDER BY rc.order ASC")
List<RouteCondition> findByRouteId(@Param("routeId") UUID routeId);
```
**Replace with**:
```java
List<RouteCondition> findByFlowRouteIdOrderByOrderAsc(UUID routeId);
```

## Queries That Should Remain Custom

### Complex Queries with Business Logic
1. **IntegrationFlowRepository** - Update queries with complex conditions
2. **AlertRepository** - Complex joins and grouping
3. **AuditTrailRepository** - Complex search conditions
4. **ExternalAuthenticationRepository** - Token refresh logic
5. **NotificationChannelRepository** - Rate limiting checks

### Native SQL Queries
1. **FlowStructureRepository** - Uses native SQL with database-specific functions
2. **MessageStructureRepository** - Uses native SQL with database-specific functions

### Queries with Complex Projections
1. **IntegrationFlowRepository** - Uses constructor expressions for DTOs
2. **AlertRepository** - Group by with aggregations

### Performance-Critical Queries
1. **AdapterHealthRecordRepository** - AVG calculations
2. **FlowExecutionRepository** - AVG execution time calculations

## Recommendations

1. **Priority 1**: Replace simple queries that exactly match JPA naming conventions
2. **Priority 2**: Replace queries that only add ORDER BY clauses
3. **Keep Custom**: Queries with:
   - Complex joins
   - Aggregations (COUNT, AVG, SUM)
   - Native SQL
   - Constructor expressions
   - UPDATE/DELETE operations
   - Complex WHERE conditions

## Implementation Steps

1. Start with repositories that have the most simple queries to replace
2. Test each replacement thoroughly as JPA-generated queries might differ slightly
3. Monitor performance after replacement
4. Keep custom queries for complex business logic

## Benefits of Replacement

1. **Reduced Code**: Less custom query maintenance
2. **Type Safety**: JPA method names are compile-time checked
3. **Consistency**: Standard JPA naming conventions
4. **Readability**: Method names describe the query intent

## Risks

1. **Performance**: JPA-generated queries might not be as optimized
2. **Complexity**: Some JPA method names can become very long
3. **Testing**: Need thorough testing to ensure behavior remains the same