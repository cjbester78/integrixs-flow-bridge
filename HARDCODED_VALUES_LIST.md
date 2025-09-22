# Hardcoded Values Found in Codebase

This document lists all hardcoded values found that need to be moved to application.yml configuration.

## 1. Rate Limiting & Capacity Configuration

### RateLimitingInterceptor.java
- Rate limit capacities: 10, 20, 50, 100, 200, 500, 1000
- Refill rates and time periods
- Cleanup interval: 3600000 (1 hour)
- Max IP buckets: 10000

### BulkheadService.java
- Max concurrent calls: 10
- Max wait duration: 0

## 2. Connection Pool & Thread Configuration

### ConnectionPoolConfiguration.java
- Database pool sizes:
  - Min idle: 5
  - Max pool size: 50
  - Connection timeout: 30000
  - Idle timeout: 600000
  - Max lifetime: 1800000

### HazelcastConfig.java
- Multicast group: "224.2.2.3"
- Multicast port: 54327
- Network config timeouts and intervals

### RedisConfig.java
- Connection pool settings
- Timeout values

## 3. Timeouts & Delays

### Various Services
- Thread.sleep(1000) - Various delays
- Lock timeout: 10 seconds
- Scheduled delays: @Scheduled(fixedDelay = values)
- Retry delays and backoff periods

## 4. Data Processing Limits

### Multiple Files
- Buffer size: 8192 bytes
- String truncation: 200, 1000, 4000 characters
- Progress callbacks: 100 elements, 10240 bytes
- Minimum file size: 1000 bytes

## 5. HTTP & Network Configuration

### WebSocket and HTTP Handlers
- HTTP status ranges: 200-300, 400+, 500+
- Path prefixes: "/soap/", "/ws/", "/static/", "/assets/"
- Localhost IPs: "127.0.0.1", "::1"

## 6. Cron Schedules

### Scheduled Tasks
- Daily backup: "0 0 2 * * *"
- Cleanup tasks: "0 0 3 * * *"

## 7. Business Logic Thresholds

### Performance Monitoring
- Utilization thresholds: 0.3, 0.8, 0.95
- Alert thresholds
- Circuit breaker thresholds

## 8. Adapter-Specific Values

### SlackApiConfig.java
- Reconnect delay: 5000
- Ping interval: 30000
- Max retry attempts: 3
- Retry delay: 1000

### Other Adapters
- Polling intervals
- Batch sizes
- Queue capacities

## Priority Order for Configuration:

1. **Critical** - Connection pools, thread pools (affects system stability)
2. **High** - Rate limiting, circuit breakers (affects reliability)
3. **Medium** - Timeouts, delays, retry configuration
4. **Low** - UI strings, display limits

## Next Steps:
1. Add these values to application.yml with meaningful property names
2. Replace hardcoded values with @Value annotations
3. Group related configurations together
4. Document each configuration property