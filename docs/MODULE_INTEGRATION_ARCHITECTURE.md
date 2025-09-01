# Module Integration Architecture

## Overview

This document defines the module boundaries and integration interfaces for the Integrix Flow Bridge system after the clean architecture refactoring. Each module has clear responsibilities and communicates with other modules through well-defined interfaces.

## Module Boundaries

### 1. Backend Module
**Purpose**: Core business logic and application services
**Responsibilities**:
- User authentication and management
- Integration flow definitions
- Field mappings
- System configuration
- Business components
- Role and permission management

**External Dependencies**:
- Data-Access module (for persistence)
- Shared-Lib module (for common DTOs and utilities)
- Engine module (for flow execution)

### 2. Engine Module
**Purpose**: Flow execution and orchestration
**Responsibilities**:
- Flow execution lifecycle management
- Adapter execution coordination
- Workflow orchestration
- Message processing
- Execution monitoring

**External Dependencies**:
- Adapters module (for adapter execution)
- Backend module (for flow definitions)
- Monitoring module (for metrics and events)

### 3. Adapters Module
**Purpose**: Communication with external systems
**Responsibilities**:
- HTTP/REST adapter
- JDBC database adapter
- FTP/SFTP file adapter
- SOAP web service adapter
- Message queue adapter
- File system adapter

**External Dependencies**:
- Shared-Lib module (for common models)
- WebClient module (for inbound HTTP)
- WebServer module (for outbound HTTP)

### 4. Monitoring Module
**Purpose**: System monitoring, metrics, and alerting
**Responsibilities**:
- Event logging
- Metrics collection
- Alert management
- Performance monitoring
- Audit trail

**External Dependencies**:
- All modules (for monitoring data)

### 5. WebClient Module
**Purpose**: Inbound message handling
**Responsibilities**:
- REST API endpoints for external systems
- Message validation
- Message routing to engine
- Format transformation

**External Dependencies**:
- Engine module (for message processing)
- Backend module (for configuration)

### 6. WebServer Module
**Purpose**: Outbound HTTP/SOAP client
**Responsibilities**:
- REST client operations
- SOAP client operations
- Retry and resilience
- Service endpoint registry

**External Dependencies**:
- SOAP-Bindings module (for SOAP operations)

### 7. SOAP Bindings Module
**Purpose**: SOAP/WSDL management
**Responsibilities**:
- WSDL parsing and validation
- SOAP binding generation
- WS-Security configuration
- SOAP operation invocation

**External Dependencies**:
- None (self-contained)

### 8. Data-Access Module
**Purpose**: Database persistence layer
**Responsibilities**:
- JPA entities
- Repository implementations
- Database migrations
- Transaction management

**External Dependencies**:
- Shared-Lib module (for DTOs)

### 9. Shared-Lib Module
**Purpose**: Common utilities and models
**Responsibilities**:
- Common DTOs
- Shared enums
- Utility classes
- Constants

**External Dependencies**:
- None (base module)

## Integration Interfaces

### 1. Backend → Engine Integration
```java
public interface FlowExecutionClient {
    ExecutionResult executeFlow(String flowId, Map<String, Object> payload);
    void stopExecution(String executionId);
    ExecutionStatus getExecutionStatus(String executionId);
}
```

### 2. Engine → Adapters Integration
```java
public interface AdapterExecutionService {
    AdapterResult executeAdapter(String adapterId, AdapterContext context);
    boolean testAdapter(String adapterId);
    AdapterMetadata getAdapterMetadata(String adapterId);
}
```

### 3. Engine → Monitoring Integration
```java
public interface MonitoringClient {
    void logEvent(MonitoringEvent event);
    void recordMetric(String metricName, double value, Map<String, String> tags);
    void triggerAlert(Alert alert);
}
```

### 4. WebClient → Engine Integration
```java
public interface MessageProcessingClient {
    ProcessingResult processInboundMessage(InboundMessage message);
    ValidationResult validateMessage(String flowId, Object payload);
}
```

### 5. Adapters → WebServer Integration
```java
public interface OutboundHttpClient {
    HttpResponse executeRequest(OutboundRequest request);
    ServiceEndpoint getEndpoint(String endpointId);
}
```

### 6. WebServer → SOAP Bindings Integration
```java
public interface SoapOperationClient {
    SoapResponse invokeSoapOperation(String bindingId, SoapRequest request);
    boolean testSoapEndpoint(String bindingId);
}
```

### 7. All Modules → Monitoring Integration
```java
public interface SystemMonitor {
    void recordActivity(String module, String operation, Map<String, Object> context);
    void recordError(String module, Exception error, Map<String, Object> context);
    void recordPerformance(String module, String operation, long durationMs);
}
```

## Communication Patterns

### 1. Synchronous Communication
- REST APIs between modules
- Direct service calls within same JVM
- Request-response pattern

### 2. Asynchronous Communication
- Event bus for system events
- Message queues for flow execution
- Webhook callbacks

### 3. Data Sharing
- Shared database with clear schema boundaries
- DTOs for inter-module communication
- No direct entity sharing between modules

## Module Dependencies Graph

```
                    ┌─────────────┐
                    │ Shared-Lib  │
                    └──────┬──────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
        ▼                  ▼                  ▼
┌───────────────┐  ┌──────────────┐  ┌──────────────┐
│ Data-Access   │  │   Backend    │  │  Monitoring  │
└───────────────┘  └──────┬───────┘  └──────────────┘
                          │                  ▲
                          ▼                  │
                   ┌──────────────┐          │
                   │    Engine    │──────────┘
                   └──────┬───────┘
                          │
        ┌─────────────────┼─────────────────┐
        │                 │                 │
        ▼                 ▼                 ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│   Adapters   │  │  WebClient   │  │  WebServer   │
└──────────────┘  └──────────────┘  └──────┬───────┘
                                           │
                                           ▼
                                    ┌──────────────┐
                                    │ SOAP Bindings│
                                    └──────────────┘
```

## Integration Guidelines

### 1. Interface Versioning
- All integration interfaces must be versioned
- Backward compatibility for at least 2 versions
- Clear deprecation notices

### 2. Error Handling
- Consistent error codes across modules
- Detailed error messages for debugging
- Circuit breaker pattern for resilience

### 3. Security
- Authentication required for all inter-module calls
- Authorization based on module roles
- Audit trail for sensitive operations

### 4. Performance
- Connection pooling for database access
- Caching for frequently accessed data
- Async processing for long operations

### 5. Testing
- Integration tests for all interfaces
- Contract testing between modules
- Mock implementations for testing

## Migration Strategy

### Phase 1: Interface Definition (Current)
- Define all integration interfaces
- Create DTOs for communication
- Document contracts

### Phase 2: Implementation
- Implement interfaces in each module
- Add integration tests
- Validate contracts

### Phase 3: Deployment
- Deploy modules incrementally
- Monitor integration points
- Optimize performance

## Monitoring Integration Points

Each integration point should monitor:
- Request/response times
- Error rates
- Throughput
- Resource usage

## Next Steps

1. Review and approve interface definitions
2. Create integration tests
3. Implement monitoring for each interface
4. Document API contracts in detail