# Clean Architecture Guide for Integrix Flow Bridge

## Overview

This guide documents the clean architecture implementation across all modules in the Integrix Flow Bridge system. Each module follows Domain-Driven Design (DDD) principles with clear separation of concerns.

## Architecture Principles

### 1. Dependency Rule
- Dependencies point inward toward the domain
- Domain layer has no external dependencies
- Infrastructure depends on domain, never vice versa
- API layer orchestrates between external requests and application services

### 2. Layer Responsibilities

#### Domain Layer
- **Purpose**: Core business logic and rules
- **Contains**: 
  - Domain models (entities, value objects)
  - Domain services (business logic)
  - Repository interfaces
- **Dependencies**: None (pure Java/Spring annotations only)

#### Application Layer
- **Purpose**: Use case orchestration
- **Contains**:
  - Application services (use case implementations)
  - Transaction boundaries
  - Cross-cutting concerns coordination
- **Dependencies**: Domain layer only

#### Infrastructure Layer
- **Purpose**: External integrations and implementations
- **Contains**:
  - Repository implementations
  - External service clients
  - Database configurations
  - Message queue implementations
- **Dependencies**: Domain and Application layers

#### API Layer
- **Purpose**: External interface (REST, GraphQL, etc.)
- **Contains**:
  - Controllers/Resources
  - DTOs (Data Transfer Objects)
  - Request/Response mappers
  - API documentation
- **Dependencies**: Application layer

## Module Structure Template

```
module-name/
├── api/
│   ├── controller/         # REST controllers
│   ├── dto/               # API DTOs
│   │   ├── request/       # Request DTOs
│   │   └── response/      # Response DTOs
│   └── mapper/            # DTO ↔ Domain mappers
├── application/
│   └── service/           # Application services
├── domain/
│   ├── model/             # Domain entities
│   ├── service/           # Domain services
│   └── repository/        # Repository interfaces
└── infrastructure/
    ├── client/            # External clients
    ├── config/            # Infrastructure config
    ├── repository/        # Repository implementations
    └── service/           # Infrastructure services
```

## Module-Specific Implementations

### Backend Module
**Primary Responsibility**: Core business operations and flow management

```java
// Domain Model Example
@Entity
@Data
@Builder
public class IntegrationFlow {
    private String id;
    private String name;
    private FlowStatus status;
    private List<FlowStep> steps;
    
    // Business methods
    public void activate() {
        if (status != FlowStatus.INACTIVE) {
            throw new IllegalStateException("Flow must be inactive to activate");
        }
        this.status = FlowStatus.ACTIVE;
    }
}

// Application Service Example
@Service
@Transactional
public class IntegrationFlowService {
    private final IntegrationFlowRepository repository;
    private final FlowValidator validator;
    
    public IntegrationFlow createFlow(CreateFlowCommand command) {
        // Orchestrate use case
        validator.validate(command);
        IntegrationFlow flow = IntegrationFlow.builder()
            .name(command.getName())
            .status(FlowStatus.INACTIVE)
            .build();
        return repository.save(flow);
    }
}
```

### Engine Module
**Primary Responsibility**: Flow execution and orchestration

```java
// Domain Service Example
@Service
public class FlowExecutor {
    public FlowExecutionResult execute(FlowExecutionContext context) {
        // Core execution logic
        try {
            validateContext(context);
            List<StepResult> results = executeSteps(context);
            return FlowExecutionResult.success(results);
        } catch (Exception e) {
            return FlowExecutionResult.failure(e.getMessage());
        }
    }
}
```

### Adapters Module
**Primary Responsibility**: External system connectivity

```java
// Port Interface
public interface AdapterPort {
    AdapterResult execute(AdapterContext context);
    boolean testConnection();
    AdapterMetadata getMetadata();
}

// Adapter Implementation
@Component
public class HttpAdapter implements AdapterPort {
    private final RestTemplate restTemplate;
    
    @Override
    public AdapterResult execute(AdapterContext context) {
        // HTTP-specific implementation
    }
}
```

### Monitoring Module
**Primary Responsibility**: System observability and alerting

```java
// Event-Driven Monitoring
@Component
public class EventLogger {
    private final EventRepository repository;
    
    @EventListener
    public void handleSystemEvent(SystemEvent event) {
        MonitoringEvent monitoringEvent = MonitoringEvent.builder()
            .timestamp(LocalDateTime.now())
            .module(event.getSource())
            .operation(event.getOperation())
            .build();
        repository.save(monitoringEvent);
    }
}
```

## Integration Patterns

### 1. Synchronous Communication
```java
// Client Interface in shared-lib
public interface FlowExecutionClient {
    ExecutionResult executeFlow(String flowId, Map<String, Object> payload);
}

// Implementation in module
@Service
public class FlowExecutionClientImpl implements FlowExecutionClient {
    private final RestTemplate restTemplate;
    
    public ExecutionResult executeFlow(String flowId, Map<String, Object> payload) {
        return restTemplate.postForObject("/flows/{id}/execute", payload, ExecutionResult.class, flowId);
    }
}
```

### 2. Asynchronous Communication
```java
// Event Publisher
@Component
public class FlowEventPublisher {
    private final KafkaTemplate<String, FlowEvent> kafkaTemplate;
    
    public void publishFlowStarted(String flowId) {
        FlowEvent event = new FlowEvent(flowId, EventType.STARTED);
        kafkaTemplate.send("flow-events", event);
    }
}

// Event Consumer
@Component
public class FlowEventConsumer {
    @KafkaListener(topics = "flow-events")
    public void handleFlowEvent(FlowEvent event) {
        // Process event
    }
}
```

## Best Practices

### 1. Domain Model Design
- Keep domain models rich with behavior
- Avoid anemic domain models
- Use value objects for immutable data
- Implement domain events for cross-aggregate communication

### 2. Service Layer Design
- Keep application services thin (orchestration only)
- Put business logic in domain services
- Use command/query separation (CQRS) where appropriate
- Handle transactions at application service level

### 3. Repository Design
- Keep repository interfaces in domain layer
- Use repository pattern for aggregate roots only
- Implement specification pattern for complex queries
- Avoid leaking persistence details into domain

### 4. API Design
- Use DTOs for external communication
- Never expose domain models directly
- Implement proper validation at API layer
- Use consistent error handling

### 5. Testing Strategy
- Unit test domain logic extensively
- Integration test application services
- Contract test module interfaces
- E2E test critical user journeys

## Migration Guide

### From Legacy to Clean Architecture

1. **Identify Boundaries**
   - Map existing service dependencies
   - Identify domain concepts
   - Define module boundaries

2. **Extract Domain Logic**
   - Move business rules to domain services
   - Create domain models with behavior
   - Define repository interfaces

3. **Refactor Services**
   - Split into application and domain services
   - Remove infrastructure concerns
   - Add proper transaction boundaries

4. **Create Infrastructure Layer**
   - Implement repository interfaces
   - Move external integrations
   - Configure frameworks

5. **Build API Layer**
   - Create DTOs and mappers
   - Implement controllers
   - Add validation and error handling

## Common Pitfalls and Solutions

### Pitfall 1: Leaking Domain Logic
**Problem**: Business logic in controllers or repositories
**Solution**: Move logic to domain services, keep controllers thin

### Pitfall 2: Anemic Domain Models
**Problem**: Domain models with only getters/setters
**Solution**: Add business methods to domain models

### Pitfall 3: Circular Dependencies
**Problem**: Modules depending on each other
**Solution**: Use interfaces and dependency inversion

### Pitfall 4: Over-Engineering
**Problem**: Too many layers for simple operations
**Solution**: Apply YAGNI principle, add complexity when needed

## Metrics for Success

1. **Code Organization**
   - Clear module boundaries
   - Consistent structure across modules
   - No circular dependencies

2. **Testability**
   - High unit test coverage (80%+)
   - Easy to mock dependencies
   - Fast test execution

3. **Maintainability**
   - Easy to add new features
   - Clear where to make changes
   - Minimal ripple effects

4. **Performance**
   - No significant overhead from architecture
   - Efficient data access patterns
   - Proper caching strategies