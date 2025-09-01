# Target Clean Architecture

## High-Level Module Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Frontend UI                           │
└────────────────────────┬────────────────────────────────────┘
                         │ REST API / WebSocket
┌────────────────────────▼────────────────────────────────────┐
│                      API Gateway                             │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────┐ ┌─────────┐ ┌──────────┐ ┌─────────────────┐ │
│  │ Backend │ │ Engine  │ │ Adapters │ │ Monitoring      │ │
│  │ Module  │ │ Module  │ │ Module   │ │ Module          │ │
│  └────┬────┘ └────┬────┘ └────┬─────┘ └────┬────────────┘ │
│       │           │            │             │              │
│  ┌────▼────┐ ┌───▼────┐ ┌────▼─────┐ ┌────▼────────────┐ │
│  │WebClient│ │WebServer│ │   SOAP   │ │ Infrastructure  │ │
│  │ Module  │ │ Module  │ │ Bindings │ │ Services        │ │
│  └─────────┘ └─────────┘ └──────────┘ └─────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│                    Shared Libraries                          │
│  ┌─────────────┐        ┌──────────────────┐              │
│  │ shared-lib  │        │ data-access      │              │
│  │ (DTOs)      │        │ (Entities)       │              │
│  └─────────────┘        └──────────────────┘              │
└─────────────────────────────────────────────────────────────┘
                                │
                    ┌───────────▼───────────┐
                    │      Database         │
                    │    (PostgreSQL)       │
                    └───────────────────────┘
```

## Module Internal Architecture (Clean Architecture)

Each module follows this structure:

```
module-name/
├── api/                    # Presentation Layer
│   ├── controller/         # REST controllers
│   ├── dto/               # Request/Response DTOs
│   │   ├── request/
│   │   └── response/
│   └── websocket/         # WebSocket handlers
│
├── application/            # Application Layer
│   ├── service/           # Use case orchestration
│   ├── command/           # Command objects
│   ├── query/             # Query objects
│   └── event/             # Application events
│
├── domain/                # Domain Layer
│   ├── model/             # Domain entities
│   ├── service/           # Domain services
│   ├── repository/        # Repository interfaces
│   ├── event/             # Domain events
│   └── exception/         # Domain exceptions
│
└── infrastructure/        # Infrastructure Layer
    ├── repository/        # Repository implementations
    ├── messaging/         # Message queues
    ├── external/          # External service clients
    ├── persistence/       # Database specific
    └── config/            # Infrastructure config
```

## Dependency Flow Rules

```
┌─────────────┐
│     API     │ ─────► Uses DTOs from shared-lib
└──────┬──────┘
       │ depends on
       ▼
┌─────────────┐
│ Application │ ─────► Orchestrates use cases
└──────┬──────┘
       │ depends on
       ▼
┌─────────────┐
│   Domain    │ ─────► Pure business logic
└──────┬──────┘        (No framework dependencies)
       │ depends on
       ▼
┌─────────────┐
│Infrastructure│ ─────► Implements domain interfaces
└─────────────┘        (Framework-specific code)
```

## Module Interaction Patterns

### 1. Backend ↔ Engine Communication
```java
// Backend Module (Application Layer)
@Service
public class FlowExecutionApplicationService {
    private final EngineClient engineClient; // From infrastructure
    
    public ExecutionResult executeFlow(String flowId) {
        // Orchestrate flow execution via engine module
        return engineClient.execute(new ExecuteFlowCommand(flowId));
    }
}

// Engine Module (API Layer)
@RestController
@RequestMapping("/api/engine")
public class EngineController {
    private final FlowExecutionUseCase useCase;
    
    @PostMapping("/execute")
    public ExecutionResponse execute(@RequestBody ExecuteFlowCommand command) {
        return useCase.execute(command);
    }
}
```

### 2. Adapter Registry Pattern
```java
// Adapters Module (Domain Layer)
public interface AdapterRegistry {
    void register(AdapterType type, AdapterFactory factory);
    Adapter create(AdapterType type, Configuration config);
}

// Adapters Module (Infrastructure Layer)
@Component
public class AdapterRegistryImpl implements AdapterRegistry {
    private final Map<AdapterType, AdapterFactory> factories;
    // Implementation details
}
```

### 3. Event-Driven Communication
```java
// Shared Events (shared-lib)
public class FlowExecutionCompletedEvent {
    private String flowId;
    private String executionId;
    private ExecutionStatus status;
}

// Engine Module publishes
eventPublisher.publish(new FlowExecutionCompletedEvent(...));

// Monitoring Module subscribes
@EventListener
public void onFlowExecutionCompleted(FlowExecutionCompletedEvent event) {
    // Update metrics
}
```

## Cross-Cutting Concerns

### 1. Security
- Implemented at API layer (controllers)
- JWT validation in API Gateway
- Role-based access control

### 2. Logging
- Structured logging with correlation IDs
- Domain layer logs business events
- Infrastructure layer logs technical details

### 3. Monitoring
- Metrics collection via AOP
- Health checks at infrastructure layer
- Performance monitoring

### 4. Transaction Management
- Handled at Application layer
- Domain remains transaction-agnostic

## Benefits of Target Architecture

1. **Testability**: Each layer can be tested independently
2. **Maintainability**: Clear separation of concerns
3. **Flexibility**: Easy to swap infrastructure components
4. **Scalability**: Modules can be deployed independently
5. **Team Autonomy**: Teams can work on modules independently

## Migration Strategy

1. **Incremental Refactoring**: One module at a time
2. **Backward Compatibility**: Maintain old APIs during transition
3. **Feature Flags**: Toggle between old and new implementations
4. **Parallel Run**: Run old and new systems side-by-side
5. **Gradual Cutover**: Switch traffic gradually to new modules