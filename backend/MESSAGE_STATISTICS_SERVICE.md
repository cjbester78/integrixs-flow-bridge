# Message Statistics Service

## Overview

The Message Statistics Service provides real-time and historical analytics for message processing within the Integrixs Flow Bridge system. It tracks message counts by status, calculates processing times, and provides success rate metrics.

## Features

- **Real-time Statistics**: Current message counts by status
- **Success Rate Calculation**: Percentage of successfully processed messages
- **Processing Time Analytics**: Average time from receipt to completion
- **Flow-specific Statistics**: Metrics filtered by integration flow
- **Time Period Analysis**: Statistics for specific date ranges
- **Optimized Queries**: Database-level aggregation for performance

## Implementation Details

### Two Service Implementations

1. **MessageStatsService**: Basic implementation with Java-based calculations
2. **OptimizedMessageStatsService**: Performance-optimized with native SQL queries (Primary)

### Message Status Categories

- **Processing**: Messages currently being processed
- **Successful**: PROCESSED or COMPLETED status
- **Failed**: FAILED or DEAD_LETTER status
- **Total**: All messages in the system

### Processing Time Calculation

Average processing time is calculated as:
- Start Time: `receivedAt`
- End Time: `completedAt` (preferred) or `processedAt`
- Result: Average duration in milliseconds

## API Usage

### Get Overall Statistics
```java
@Autowired
private MessageStatsService messageStatsService;

// Get overall stats
MessageStatsDTO stats = messageStatsService.getMessageStats(null);

// Response:
// {
//   "total": 1000,
//   "successful": 850,
//   "processing": 50,
//   "failed": 100,
//   "successRate": 89.47,
//   "avgProcessingTime": 1250.5
// }
```

### Get Flow-Specific Statistics
```java
// Stats for a specific flow
Map<String, String> filters = Map.of("flowId", flowId.toString());
MessageStatsDTO flowStats = messageStatsService.getMessageStats(filters);
```

### Get Time Period Statistics
```java
// Stats for last 24 hours
LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
LocalDateTime now = LocalDateTime.now();
MessageStatsDTO periodStats = messageStatsService.getStatsForPeriod(yesterday, now);
```

### Get Real-time Statistics (Last Hour)
```java
// For dashboard displays
OptimizedMessageStatsService optimizedService = 
    (OptimizedMessageStatsService) messageStatsService;
MessageStatsDTO realtimeStats = optimizedService.getRealtimeStats();
```

### Get Status Breakdown
```java
// Detailed count by each status
Map<String, Long> breakdown = optimizedService.getStatusBreakdown();
// {
//   "PENDING": 10,
//   "PROCESSING": 50,
//   "PROCESSED": 800,
//   "COMPLETED": 50,
//   "FAILED": 90,
//   "DEAD_LETTER": 10
// }
```

## Database Queries

### Optimized Native Queries

1. **Count by Status (Grouped)**:
```sql
SELECT m.status, COUNT(m) 
FROM Message m 
GROUP BY m.status
```

2. **Average Processing Time**:
```sql
SELECT AVG(EXTRACT(EPOCH FROM (m.completedAt - m.receivedAt)) * 1000) 
FROM Message m 
WHERE m.status IN ('PROCESSED', 'COMPLETED') 
AND m.receivedAt IS NOT NULL 
AND m.completedAt IS NOT NULL
```

3. **Flow-Specific Average**:
```sql
SELECT AVG(EXTRACT(EPOCH FROM (m.completedAt - m.receivedAt)) * 1000) 
FROM Message m 
WHERE m.status IN ('PROCESSED', 'COMPLETED') 
AND m.flow.id = :flowId 
AND m.receivedAt IS NOT NULL 
AND m.completedAt IS NOT NULL
```

## Performance Considerations

### Optimizations

1. **Grouped Queries**: Single query for all status counts
2. **Database Aggregation**: Calculations done at DB level
3. **Indexed Columns**: Status and timestamp fields should be indexed
4. **Read-Only Transactions**: All queries use read-only mode

### Recommended Indexes

```sql
CREATE INDEX idx_message_status ON messages(status);
CREATE INDEX idx_message_received_at ON messages(received_at);
CREATE INDEX idx_message_completed_at ON messages(completed_at);
CREATE INDEX idx_message_flow_status ON messages(flow_id, status);
```

## REST API Endpoints

### Get Message Statistics
```http
GET /api/stats/messages

Response:
{
  "total": 1000,
  "successful": 850,
  "processing": 50,
  "failed": 100,
  "successRate": 89.47,
  "avgProcessingTime": 1250.5
}
```

### Get Flow Statistics
```http
GET /api/stats/messages/flow/{flowId}

Response: Same as above but filtered by flow
```

### Get Period Statistics
```http
GET /api/stats/messages/period?start=2024-01-01T00:00:00&end=2024-01-31T23:59:59

Response: Statistics for the specified time period
```

## Monitoring Integration

The statistics service integrates with:
- **Dashboards**: Real-time message processing metrics
- **Alerts**: Trigger alerts based on failure rates
- **Reports**: Historical performance analysis
- **SLAs**: Track processing time compliance

## Configuration

### Application Properties
```yaml
# Statistics calculation settings
integrix:
  stats:
    cache:
      enabled: true
      ttl: 60  # Cache TTL in seconds
    calculation:
      batch-size: 1000  # Batch size for period calculations
```

## Error Handling

- Returns zero-initialized DTO on database errors
- Logs errors for debugging
- Null-safe processing time calculations
- Handles missing timestamp data gracefully

## Future Enhancements

1. **Caching**: Redis cache for frequently accessed stats
2. **Streaming Analytics**: Real-time updates via WebSocket
3. **Custom Metrics**: User-defined statistical calculations
4. **Predictive Analytics**: ML-based performance predictions
5. **Detailed Breakdowns**: Stats by message type, source system, etc.