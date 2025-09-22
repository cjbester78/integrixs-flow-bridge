# Hexagonal Architecture Patterns in Integrixs Flow Bridge

## Overview

This document describes the hexagonal architecture (ports and adapters) patterns implemented in the Integrixs Flow Bridge project. These patterns ensure clean separation of concerns, testability, and flexibility in implementation choices.

## Core Concepts

### 1. Ports (Domain Interfaces)
Ports define the contracts for how the domain interacts with external systems. They are pure interfaces that belong to the domain layer and have no dependencies on infrastructure concerns.

### 2. Adapters (Infrastructure Implementations)
Adapters implement the port interfaces and handle the actual integration with external systems like databases, message queues, or web services.

## Repository Pattern Implementation

### Domain Layer (Ports)
All repository interfaces in the domain layer follow the Port suffix naming convention:

```
backend/src/main/java/com/integrixs/backend/domain/repository/
├── UserRepositoryPort.java
├── SystemLogRepositoryPort.java
├── SystemConfigurationRepositoryPort.java
├── MessageRepositoryPort.java
├── IntegrationFlowRepositoryPort.java
├── CommunicationAdapterRepositoryPort.java
├── BusinessComponentRepositoryPort.java
├── CertificateRepositoryPort.java
├── RoleRepositoryPort.java
├── FieldMappingRepositoryPort.java
├── DomainFlowExecutionRepositoryPort.java
├── DomainMessageRepositoryPort.java
├── FlowTransformationDomainRepositoryPort.java
└── FlowTransformationRepositoryPort.java
```

#### Example Port Interface:
```java
package com.integrixs.backend.domain.repository;

/**
 * User repository port - domain layer
 * Acts as a port in hexagonal architecture for user persistence operations
 */
public interface UserRepositoryPort {
    Optional<User> findById(UUID id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    User save(User user);
    void delete(User user);
    long countByRole(String role);
}
```

### Infrastructure Layer (Adapters)
Repository implementations in the infrastructure layer implement the port interfaces:

```
backend/src/main/java/com/integrixs/backend/infrastructure/persistence/
├── UserRepositoryImpl.java
├── SystemLogRepositoryImpl.java
├── SystemConfigurationRepositoryImpl.java
├── MessageRepositoryImpl.java (also in infrastructure/repository/)
├── IntegrationFlowRepositoryImpl.java
├── CommunicationAdapterRepositoryImpl.java
├── BusinessComponentRepositoryImpl.java
├── CertificateRepositoryImpl.java
├── RoleRepositoryImpl.java
├── FieldMappingRepositoryImpl.java
├── DomainFlowExecutionRepositoryImpl.java
├── DomainMessageRepositoryImpl.java
├── FlowTransformationRepositoryImpl.java
└── FlowTransformationRepositoryImplementation.java
```

#### Example Adapter Implementation:
```java
package com.integrixs.backend.infrastructure.persistence;

@Repository("backendUserRepository")
public class UserRepositoryImpl implements UserRepositoryPort {
    
    private final com.integrixs.data.repository.UserRepository jpaRepository;
    
    public UserRepositoryImpl(com.integrixs.data.repository.UserRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id);
    }
    // ... other method implementations
}
```

### Data Access Layer (JPA Repositories)
The actual JPA repositories reside in the data-access module:

```
data-access/src/main/java/com/integrixs/data/repository/
├── UserRepository.java (extends JpaRepository)
├── SystemLogRepository.java
├── SystemConfigurationRepository.java
└── ... (other JPA repositories)
```

## Service Pattern Implementation

### Domain Services
Services in the domain layer follow a similar pattern, with renamed services to clarify their purpose:

#### Messaging Services:
- **WebMessageRoutingService** (webclient module) - Routes web-based messages
- **InterModuleRoutingService** (shared-lib module) - Inter-module message routing interface
- **MessagingQueueRoutingService** (backend module) - Queue-based message routing
- **FlowMessageProcessor** (engine module) - Process messages within flows

#### Alerting Services:
- **FlowAlertingService** (backend module) - Flow-specific alerting
- **MonitoringAlertService** (monitoring module) - System monitoring alerts
- **FlowAlertingPort** (engine module) - Alerting port interface

## Benefits of This Architecture

### 1. Testability
- Domain logic can be tested in isolation using mock implementations of ports
- Infrastructure adapters can be tested separately with integration tests

### 2. Flexibility
- Easy to swap implementations (e.g., change from JPA to MongoDB)
- Multiple implementations can coexist (e.g., caching adapter wrapper)

### 3. Clear Boundaries
- Domain logic is protected from infrastructure concerns
- Dependencies flow inward (infrastructure depends on domain, not vice versa)

### 4. Maintainability
- Changes to infrastructure don't affect domain logic
- Clear separation makes the codebase easier to understand

## Migration Guidelines

When adding new repositories or services:

### 1. Create the Port Interface
```java
public interface NewFeatureRepositoryPort {
    // Define domain operations
}
```

### 2. Implement the Adapter
```java
@Repository("domainNewFeatureRepository")
public class NewFeatureRepositoryImpl implements NewFeatureRepositoryPort {
    private final JpaNewFeatureRepository jpaRepository;
    
    public NewFeatureRepositoryImpl(JpaNewFeatureRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    // Implement methods
}
```

### 3. Use Dependency Injection
```java
@Service
public class NewFeatureService {
    private final NewFeatureRepositoryPort repository;
    
    public NewFeatureService(NewFeatureRepositoryPort repository) {
        this.repository = repository;
    }
}
```

## Naming Conventions

### Repositories:
- **Port Interface**: `{Entity}RepositoryPort` (e.g., UserRepositoryPort)
- **Implementation**: `{Entity}RepositoryImpl` (e.g., UserRepositoryImpl)
- **JPA Repository**: `{Entity}Repository` (e.g., UserRepository extends JpaRepository)

### Services:
- Use descriptive names that indicate the service's specific responsibility
- Avoid generic names like "MessageRoutingService" when multiple exist
- Examples:
  - WebMessageRoutingService (for web-specific routing)
  - FlowAlertingService (for flow-specific alerts)
  - MonitoringAlertService (for monitoring-specific alerts)

## Module Dependencies

```
┌─────────────────┐
│   Domain Layer  │ (Contains Ports)
│  (backend/domain)│
└────────▲────────┘
         │
┌────────┴────────┐
│Infrastructure   │ (Contains Adapters)
│(backend/infra)  │
└────────▲────────┘
         │
┌────────┴────────┐
│  Data Access    │ (Contains JPA Repositories)
│ (data-access)   │
└─────────────────┘
```

## Conclusion

This hexagonal architecture provides a robust foundation for the Integrixs Flow Bridge project, ensuring:
- Clean separation of business logic from technical details
- Easy testing and mocking of external dependencies
- Flexibility to change implementations without affecting business logic
- Clear, understandable code organization

All future development should follow these patterns to maintain architectural consistency.