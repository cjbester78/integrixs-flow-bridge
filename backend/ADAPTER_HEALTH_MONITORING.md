# Adapter Health Monitoring

## Overview

The Adapter Health Monitor performs automated health checks on all active communication adapters, tracking their availability, response times, and error rates. It supports various adapter types with specific health check implementations.

## Features

- **Automated Health Checks**: Periodic checks every 30 seconds (configurable)
- **Adapter-Specific Checks**: Custom health verification for each adapter type
- **Metrics Collection**: Response times, success rates, connection pool status
- **Failure Tracking**: Consecutive failure counting with configurable thresholds
- **Health History**: Persistent storage of health check results

## Supported Adapter Types

### 1. HTTP/REST Adapters
- Sends GET/POST requests to configured health endpoints
- Supports basic authentication
- Configurable timeout and custom health paths
- Response code validation (2xx = healthy)

### 2. Database Adapters (JDBC)
- Executes validation queries (default: `SELECT 1`)
- Tests connection establishment
- Supports all JDBC-compatible databases
- Configurable query timeout

### 3. File System Adapters
- Verifies directory existence
- Checks read/write permissions based on adapter mode
- Fast local file system validation

### 4. FTP/SFTP Adapters
- **FTP**: Login verification and working directory check
- **SFTP**: SSH connection and directory listing
- Support for anonymous and authenticated connections
- Configurable connection timeout

### 5. JMS Adapters (Message Queues)
- Connection factory creation
- Queue/Topic accessibility verification
- Currently supports ActiveMQ
- Tests message consumer creation

### 6. SOAP Adapters
- WSDL accessibility check
- Basic SOAP envelope exchange
- Supports authenticated endpoints
- Accepts SOAP faults as "healthy" (service is responding)

## Configuration

### Application Properties

```yaml
integrix:
  health:
    check:
      interval: 30000        # Health check interval in milliseconds
      timeout: 10000         # Health check timeout in milliseconds
    failure:
      threshold: 3           # Consecutive failures before marking adapter as failed
```

### Adapter Configuration Examples

#### HTTP Adapter
```json
{
  "endpoint": "https://api.example.com",
  "healthPath": "/health",
  "timeout": 5000,
  "method": "GET",
  "username": "user",
  "password": "pass"
}
```

#### Database Adapter
```json
{
  "jdbcUrl": "jdbc:mysql://localhost:3306/mydb",
  "username": "dbuser",
  "password": "dbpass",
  "driverClass": "com.mysql.cj.jdbc.Driver",
  "validationQuery": "SELECT 1",
  "timeout": 5000
}
```

#### SFTP Adapter
```json
{
  "host": "sftp.example.com",
  "port": 22,
  "username": "sftpuser",
  "password": "sftppass",
  "timeout": 5000
}
```

#### JMS Adapter
```json
{
  "brokerUrl": "tcp://localhost:61616",
  "username": "admin",
  "password": "admin",
  "destinationName": "test.queue",
  "destinationType": "QUEUE",
  "timeout": 5000
}
```

#### SOAP Adapter
```json
{
  "wsdlUrl": "https://api.example.com/service?wsdl",
  "endpoint": "https://api.example.com/service",
  "username": "soapuser",
  "password": "soappass",
  "timeout": 10000
}
```

## Health Status Information

### AdapterHealthStatus
- **healthy**: Current health state (true/false)
- **active**: Whether adapter is active
- **lastCheckTime**: Timestamp of last health check
- **lastError**: Error message from last failed check
- **consecutiveFailures**: Number of consecutive failed checks
- **uptime**: Percentage of successful checks
- **averageResponseTime**: Average response time for successful checks

### Metrics
- **messagesProcessed**: Total messages successfully processed
- **messagesFailed**: Total messages that failed processing
- **activeConnections**: Current active connections from pool
- **pooledConnections**: Available pooled connections
- **averageProcessingTime**: Average message processing time
- **successRate**: Percentage of successful message processing

## API Endpoints

```http
# Get health status for all adapters
GET /api/adapters/health

# Get health status for specific adapter
GET /api/adapters/{adapterId}/health

# Force immediate health check
POST /api/adapters/{adapterId}/health/check

# Get adapter metrics
GET /api/adapters/{adapterId}/metrics
```

## Monitoring Integration

The health monitor integrates with the notification system. When an adapter exceeds the failure threshold:

1. Health status is updated in the database
2. Alert notification is triggered
3. Adapter is marked as unhealthy
4. Pool manager can take corrective action

## Database Schema

Health check results are stored in the `adapter_health_records` table:
- `adapter_id`: Reference to the adapter
- `healthy`: Boolean health status
- `response_time_ms`: Response time in milliseconds
- `error_message`: Error details if check failed
- `check_time`: Timestamp of the check

## Best Practices

1. **Health Endpoints**: Configure dedicated health endpoints for HTTP services
2. **Validation Queries**: Use lightweight queries for database checks
3. **Timeouts**: Set appropriate timeouts based on network conditions
4. **Failure Threshold**: Adjust threshold based on adapter criticality
5. **Monitoring**: Set up alerts for critical adapters

## Troubleshooting

### Common Issues

1. **False Positives**: 
   - Check network connectivity
   - Verify credentials
   - Ensure health endpoints are accessible

2. **Performance Impact**:
   - Increase check interval for non-critical adapters
   - Use connection pooling
   - Optimize validation queries

3. **Authentication Failures**:
   - Verify credentials in adapter configuration
   - Check for expired tokens/passwords
   - Ensure proper authentication method

## Future Enhancements

- Support for additional JMS providers (RabbitMQ, Kafka)
- Custom health check scripts
- Predictive failure detection
- Auto-recovery mechanisms
- Health check scheduling policies