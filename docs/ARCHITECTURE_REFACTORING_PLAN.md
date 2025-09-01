# Architecture Refactoring Plan

## Goal: Achieve Clean Layered Architecture

### Target Package Structure

```
com.integrixs.backend/
├── api/                    # REST API Layer
│   ├── controller/         # REST Controllers (HTTP endpoints only)
│   ├── dto/                # Request/Response DTOs
│   └── mapper/             # DTO ↔ Domain mappers
│
├── application/            # Application Layer
│   ├── service/            # Application Services (use cases)
│   ├── facade/             # Facades for complex orchestration
│   └── command/            # Command/Query handlers
│
├── domain/                 # Domain Layer
│   ├── model/              # Domain entities
│   ├── service/            # Domain services
│   ├── repository/         # Repository interfaces
│   └── valueobject/        # Value objects
│
├── infrastructure/         # Infrastructure Layer
│   ├── persistence/        # Repository implementations
│   ├── adapter/            # External adapters
│   ├── security/           # Security infrastructure
│   ├── monitoring/         # Monitoring/logging
│   └── config/             # Spring configuration
│
└── shared/                 # Shared Kernel
    ├── exception/          # Custom exceptions
    ├── event/              # Domain events
    ├── annotation/         # Custom annotations
    └── util/               # Utilities
```

## Refactoring Steps

### Step 1: Create New Package Structure (Day 1)

```bash
# Create new package structure
mkdir -p backend/src/main/java/com/integrixs/backend/{api/{controller,dto,mapper},application/{service,facade,command},domain/{model,service,repository,valueobject},infrastructure/{persistence,adapter,security,monitoring,config},shared/{exception,event,annotation,util}}
```

### Step 2: Move Controllers (Day 1)
**Current**: `backend.controller.*`  
**Target**: `backend.api.controller.*`

- Keep only HTTP handling logic
- Move business logic to application services
- Use DTOs for request/response

### Step 3: Create Application Services (Day 2-3)
**Current**: `backend.service.*` (54+ services!)  
**Target**: Split into:
- `backend.application.service.*` - Use case orchestration
- `backend.domain.service.*` - Domain logic

**God Services to Split**:
1. `FlowExecutionMonitoringService` → 
   - `FlowExecutionApplicationService` (orchestration)
   - `FlowMonitoringDomainService` (monitoring logic)
   - `AlertingDomainService` (alert logic)

2. `ErrorHandlingService` →
   - `ErrorHandlingApplicationService` (orchestration)
   - `RetryPolicyDomainService` (retry logic)
   - `DeadLetterDomainService` (dead letter logic)

### Step 4: Create Facades (Day 3)
**New**: `backend.application.facade.*`

Create facades for complex multi-service operations:
- `FlowExecutionFacade` - Orchestrates flow execution
- `AdapterManagementFacade` - Manages adapter lifecycle
- `MonitoringFacade` - Aggregates monitoring data

### Step 5: Extract Repository Interfaces (Day 4)
**Current**: Direct repository usage in services  
**Target**: 
- `backend.domain.repository.*` - Interfaces only
- `backend.infrastructure.persistence.*` - Implementations

### Step 6: Move Infrastructure (Day 4)
**Current**: Mixed with business logic  
**Target**: `backend.infrastructure.*`

- Security → `infrastructure.security`
- Adapters → `infrastructure.adapter`
- Config → `infrastructure.config`

### Step 7: Create Domain Model (Day 5)
**Current**: Using JPA entities directly  
**Target**: `backend.domain.model.*`

Options:
1. Create domain models separate from JPA entities
2. Move entities to domain layer
3. Use entities as domain models (pragmatic approach)

## Migration Strategy

### Phase 1: Structure Without Breaking (Week 1)
1. Create new package structure
2. Copy (don't move) classes to new locations
3. Create interfaces/facades
4. Gradually migrate references

### Phase 2: Clean Up (Week 2)
1. Remove old packages
2. Fix all imports
3. Update configuration
4. Run tests

## Class Mapping

### Controllers (20 files)
```
OLD: backend.controller.AuthController
NEW: backend.api.controller.AuthController
     + backend.application.service.AuthenticationService
```

### Services (54 files) - Split by Responsibility
```
Application Services (Use Cases):
- AuthService → AuthenticationApplicationService
- FlowExecutionAsyncService → FlowExecutionApplicationService
- IntegrationFlowService → IntegrationFlowApplicationService

Domain Services (Business Logic):
- FieldMappingService → FieldMappingDomainService
- AdapterExecutionService → AdapterExecutionDomainService
- TransformationExecutionService → TransformationDomainService

Infrastructure Services:
- EmailService → infrastructure.messaging.EmailService
- MessageQueueService → infrastructure.messaging.MessageQueueService
- CertificateService → infrastructure.security.CertificateService
```

### Repositories
```
OLD: Direct @Autowired Repository usage
NEW: backend.domain.repository.FlowRepository (interface)
     backend.infrastructure.persistence.FlowRepositoryImpl
```

## Benefits After Refactoring

1. **Clear Dependencies**: Each layer depends only on layers below
2. **Testability**: Can test each layer independently
3. **Maintainability**: Clear where each concern belongs
4. **Flexibility**: Can swap implementations easily

## Quick Wins

### 1. Start with One Vertical Slice
Pick one feature (e.g., Authentication) and refactor completely:
- AuthController → api.controller
- AuthService → application.service + domain.service
- UserRepository → domain.repository interface
- Create DTOs and mappers

### 2. Create Package Rules
```java
// ArchUnit test
@Test
void controllersShouldNotDependOnRepositories() {
    noClasses()
        .that().resideInPackage("..api.controller..")
        .should().dependOnClassesThat()
        .resideInPackage("..repository..")
        .check(classes);
}
```

### 3. Gradual Migration
- Don't try to refactor everything at once
- Keep old structure working while building new
- Migrate one controller/service at a time
- Use feature flags if needed

## Expected Challenges

1. **Circular Dependencies**: Will surface during refactoring
2. **Transaction Boundaries**: May need to adjust
3. **Spring Injection**: May need to update configurations
4. **Testing**: Will need to update test structure

## Success Criteria

- [ ] All controllers in api.controller package
- [ ] Clear separation between application and domain services
- [ ] No direct repository usage in controllers
- [ ] All DTOs in api.dto package
- [ ] Facades handle complex orchestration
- [ ] Domain logic isolated in domain layer
- [ ] Infrastructure concerns separated
- [ ] Compilation successful
- [ ] All tests passing