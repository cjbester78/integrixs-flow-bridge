# Integrix Flow Bridge - Architecture Documentation

## Overview

Integrix Flow Bridge is an enterprise integration platform built on a multi-module Maven architecture. This document provides a comprehensive overview of the project structure, module responsibilities, and class organization to guide development and prevent duplicate implementations.

## Project Structure

```
integrixs-flow-bridge/
├── backend/              # Main Spring Boot application
├── adapters/            # Integration adapter implementations
├── data-access/         # JPA entities and repositories
├── engine/              # Flow execution and transformation
├── frontend/            # React/TypeScript web application
├── monitoring/          # System monitoring components
├── shared-lib/          # Common utilities and DTOs
├── soap-bindings/       # SOAP/WSDL support
├── webclient/           # Web client endpoints
├── webserver/           # Web server components
├── db/                  # Database migrations
├── integration-tests/   # End-to-end tests
└── plugin-archetype/    # Plugin template
```

## Module Responsibilities and Guidelines

### 1. Backend Module (`backend/`)
**Purpose**: Main Spring Boot application hosting REST APIs and core business logic

**Responsibilities**:
- REST API controllers and endpoints
- Application services orchestrating business logic
- Security configuration (JWT, authentication, authorization)
- WebSocket endpoints for real-time communication
- Configuration management
- Exception handling and global error responses
- Background job processing
- Audit logging and business operation tracking

**When to add classes here**:
- New REST endpoints or API versions
- Application-level service orchestration
- Security filters or authentication mechanisms
- WebSocket handlers
- Global configuration classes
- Background job executors

**Key Packages**:
- `api.controller` - REST controllers
- `application.service` - Application services
- `domain.service` - Domain services
- `infrastructure` - Infrastructure implementations
- `security` - Security components
- `config` - Configuration classes

### 2. Adapters Module (`adapters/`)
**Purpose**: All integration adapter implementations for external system connectivity

**Responsibilities**:
- Adapter implementations (File, FTP, HTTP, SOAP, Database, etc.)
- Social media integrations (Facebook, Twitter, LinkedIn, etc.)
- Messaging system adapters (Kafka, RabbitMQ, SMS)
- Adapter configuration classes
- Connection testing utilities
- Retry and resilience mechanisms

**When to add classes here**:
- New adapter type implementations
- Adapter configuration classes
- Adapter-specific utilities
- Connection/authentication handlers for external systems

**Key Packages**:
- `infrastructure.adapter` - Core adapter implementations
- `social.*` - Social media adapters
- `messaging.*` - Messaging system adapters
- `collaboration.*` - Collaboration tool adapters
- `config` - Adapter configuration classes
- `core` - Base adapter abstractions

### 3. Data-Access Module (`data-access/`)
**Purpose**: Database entities, repositories, and data access logic

**Responsibilities**:
- JPA entity definitions
- Repository interfaces and implementations
- Database query specifications
- Entity relationships and mappings
- Audit fields and base entities

**When to add classes here**:
- New database entities
- Repository interfaces
- Custom query implementations
- Database-specific utilities
- Entity listeners or converters

**Key Entities**:
- `BaseEntity` - Base class with audit fields
- `IntegrationFlow` - Flow definitions
- `CommunicationAdapter` - Adapter configurations
- `Message` - Message tracking
- `User`, `Role` - Security entities
- `BusinessComponent` - Multi-tenancy

### 4. Engine Module (`engine/`)
**Purpose**: Flow execution engine and data transformation logic

**Responsibilities**:
- Flow execution orchestration
- Field mapping and transformation
- Format conversions (XML, JSON, CSV, etc.)
- Transformation function execution
- Message routing logic
- Adapter invocation framework

**When to add classes here**:
- New transformation functions
- Format converters
- Flow execution strategies
- Mapping processors
- Router implementations

**Key Packages**:
- `domain.service` - Execution services
- `transformation` - Transformation logic
- `xml` - XML conversion utilities
- `mapper` - Field mapping processors
- `service` - Adapter-specific services

### 5. Frontend Module (`frontend/`)
**Purpose**: React/TypeScript web application

**Responsibilities**:
- User interface components
- State management (TanStack Query, Zustand)
- API integration services
- Visual flow editors
- Form generation and validation

**When to add files here**:
- New UI components
- New pages or routes
- API service integrations
- Custom hooks
- Type definitions

**Key Directories**:
- `components/` - Reusable UI components
- `pages/` - Route-based page components
- `services/` - API integration services
- `hooks/` - Custom React hooks
- `types/` - TypeScript type definitions

### 6. Monitoring Module (`monitoring/`)
**Purpose**: System monitoring, metrics, and alerting

**Responsibilities**:
- Performance metrics collection
- Alert rule evaluation
- Monitoring dashboards data
- System health indicators
- Audit trail management

**When to add classes here**:
- New monitoring metrics
- Alert rule implementations
- Performance analyzers
- Health check indicators

### 7. Shared-Lib Module (`shared-lib/`)
**Purpose**: Common utilities and shared DTOs across modules

**Responsibilities**:
- Common DTOs and value objects
- Utility classes used across modules
- Shared constants and enums
- Common exception types
- Shared configuration models

**When to add classes here**:
- DTOs used by multiple modules
- Common utility functions
- Shared constants or enums
- Cross-module exception types

### 8. Supporting Modules

#### SOAP-Bindings Module (`soap-bindings/`)
- WSDL parsing and generation
- SOAP message handling
- XSD schema processing

#### WebClient Module (`webclient/`)
- Client-side web service utilities
- Inbound message schemas

#### WebServer Module (`webserver/`)
- Web service endpoints
- Server-side handlers

#### DB Module (`db/`)
- Flyway migration scripts
- Seed data
- Database documentation

## Class Organization Guidelines

### Naming Conventions

1. **Controllers**: `*Controller.java`
   - Location: `backend/api/controller/`
   - Example: `FlowExecutionController.java`

2. **Services**: 
   - Application Services: `*ApplicationService.java`
   - Domain Services: `*Service.java`
   - Infrastructure Services: `*ServiceImpl.java`

3. **Entities**: Plain names
   - Location: `data-access/model/`
   - Example: `IntegrationFlow.java`

4. **Repositories**: `*Repository.java`
   - Interface: `data-access/repository/`
   - Implementation: `backend/infrastructure/persistence/`

5. **DTOs**: 
   - Request: `*Request.java`
   - Response: `*Response.java`
   - General: `*DTO.java`

6. **Configurations**: `*Config.java` or `*Configuration.java`

### Package Structure Best Practices

1. **Layered Architecture**:
   ```
   api/          # Presentation layer
   application/  # Application layer
   domain/       # Domain layer
   infrastructure/ # Infrastructure layer
   ```

2. **Feature Grouping**: Group related classes by feature
   ```
   adapters/social/facebook/
   ├── FacebookGraphApiConfig.java
   ├── FacebookGraphInboundAdapter.java
   ├── FacebookGraphOutboundAdapter.java
   └── model/
       └── FacebookPost.java
   ```

3. **Separation of Concerns**:
   - Keep domain logic in domain services
   - Keep infrastructure concerns in infrastructure layer
   - Keep API contracts in api layer

## Module Dependencies

```
frontend ──┐
           ├──> backend ──> data-access
           │       │
           │       ├──> engine
           │       │
           │       ├──> adapters
           │       │
           │       ├──> monitoring
           │       │
           │       └──> shared-lib
           │
           └──> shared-lib

All modules depend on shared-lib for common utilities
```

## Adding New Features

### 1. New Adapter Type
1. Create config class in `adapters/config/`
2. Create inbound/outbound implementations in `adapters/infrastructure/adapter/`
3. Add factory support in `adapters/factory/`
4. Create frontend configuration component in `frontend/components/adapter/`
5. Register in `frontend/pages/CreateCommunicationAdapter.tsx`
6. Add database migration if needed

### 2. New API Endpoint
1. Create controller in `backend/api/controller/`
2. Create DTOs in `backend/api/dto/`
3. Create application service in `backend/application/service/`
4. Add domain logic in `backend/domain/service/`
5. Create frontend service in `frontend/services/`
6. Add TypeScript types in `frontend/types/`

### 3. New Entity
1. Create entity in `data-access/model/`
2. Create repository interface in `data-access/repository/`
3. Create repository implementation in `backend/infrastructure/persistence/`
4. Add Flyway migration in `backend/resources/db/migration/`

### 4. New Transformation Function
1. Create function in `engine/transformation/`
2. Register in transformation executor
3. Add to function registry
4. Create UI component in `frontend/components/development/`

## Important Notes

1. **No Duplicate Classes**: Check existing modules before creating new classes
2. **Follow Module Boundaries**: Don't add classes to wrong modules
3. **Use Shared-Lib**: For truly shared components across modules
4. **Maintain Consistency**: Follow existing patterns and naming conventions
5. **Update Documentation**: Keep this document updated with significant changes

## Module Class Counts (Approximate)

- Backend: ~400 classes
- Adapters: ~200 classes
- Data-Access: ~100 classes
- Engine: ~75 classes
- Shared-Lib: ~115 classes
- Monitoring: ~45 classes

Total Backend Classes: ~935

## Frontend Structure

The frontend uses a feature-based folder structure:

```
frontend/src/
├── components/     # Reusable UI components
│   ├── adapter/   # Adapter configuration components
│   ├── flow/      # Flow-related components
│   ├── layout/    # Layout components
│   └── ui/        # Base UI components (shadcn)
├── pages/         # Route-based pages
├── services/      # API integration services
├── hooks/         # Custom React hooks
├── types/         # TypeScript definitions
├── stores/        # Zustand state stores
└── lib/           # Utilities and helpers
```

## Database Schema Patterns

- **UUID Primary Keys**: All tables use UUID for primary keys
- **Audit Columns**: created_at, updated_at, created_by, updated_by
- **JSONB Configuration**: Flexible configuration storage
- **Soft Deletes**: Implemented where appropriate
- **Multi-tenancy**: Through business_component relationships

## Appendix: Detailed Class Listings

### Backend Module Classes (Key Classes)

#### API Controllers
- `AuthController` - Authentication endpoints
- `BusinessComponentController` - Multi-tenant business components
- `CommunicationAdapterController` - Adapter management
- `FlowExecutionController` - Flow execution endpoints
- `IntegrationFlowController` - Flow management
- `MessageController` - Message tracking
- `SystemConfigController` - System configuration
- `DashboardController` - Dashboard metrics

#### Application Services
- `AuthenticationService` - User authentication
- `CommunicationAdapterService` - Adapter orchestration
- `FlowExecutionApplicationService` - Flow execution orchestration
- `IntegrationFlowService` - Flow management
- `MessageQueryService` - Message search and retrieval

#### Domain Services
- `FlowExecutionService` - Core flow execution logic
- `MessageProcessingService` - Message handling
- `AdapterConfigurationService` - Adapter configuration
- `RoutingManagementService` - Message routing
- `FlowValidationService` - Flow validation logic

### Adapters Module Classes (Complete List)

#### Core Adapter Classes
- `AbstractAdapter` - Base adapter implementation
- `AbstractInboundAdapter` - Base inbound adapter
- `AbstractOutboundAdapter` - Base outbound adapter
- `BaseAdapter` - Adapter interface
- `InboundAdapter` - Inbound interface
- `OutboundAdapter` - Outbound interface
- `AdapterMonitoringService` - Adapter monitoring
- `ConnectionTestUtil` - Connection testing

#### Infrastructure Adapters
- `FileInboundAdapter`, `FileOutboundAdapter` - File system
- `FtpInboundAdapter`, `FtpOutboundAdapter` - FTP
- `HttpInboundAdapter`, `HttpOutboundAdapter` - HTTP/REST
- `SoapInboundAdapter`, `SoapOutboundAdapter` - SOAP web services
- `JdbcInboundAdapter`, `JdbcOutboundAdapter` - Database
- `KafkaInboundAdapter`, `KafkaOutboundAdapter` - Kafka messaging
- `MailInboundAdapter`, `MailOutboundAdapter` - Email
- `SftpInboundAdapter`, `SftpOutboundAdapter` - SFTP
- `IbmmqInboundAdapter`, `IbmmqOutboundAdapter` - IBM MQ
- `IdocInboundAdapter`, `IdocOutboundAdapter` - SAP IDoc
- `RfcInboundAdapter`, `RfcOutboundAdapter` - SAP RFC
- `OdataInboundAdapter`, `OdataOutboundAdapter` - OData

#### Social Media Adapters
- Facebook: `FacebookGraphInboundAdapter`, `FacebookGraphOutboundAdapter`, `FacebookAdsInboundAdapter`, `FacebookMessengerInboundAdapter`
- Twitter: `TwitterApiV2InboundAdapter`, `TwitterApiV2OutboundAdapter`, `TwitterAdsInboundAdapter`
- LinkedIn: `LinkedInInboundAdapter`, `LinkedInOutboundAdapter`, `LinkedInAdsInboundAdapter`
- Instagram: `InstagramGraphInboundAdapter`, `InstagramGraphOutboundAdapter`
- YouTube: `YouTubeDataInboundAdapter`, `YouTubeAnalyticsInboundAdapter`
- WhatsApp: `WhatsAppBusinessInboundAdapter`, `WhatsAppBusinessOutboundAdapter`
- TikTok: `TikTokContentInboundAdapter`, `TikTokBusinessInboundAdapter`
- Others: Discord, Telegram, Reddit, Pinterest, Snapchat

#### Messaging Adapters
- `RabbitMQInboundAdapter`, `RabbitMQOutboundAdapter`
- `AMQPInboundAdapter`, `AMQPOutboundAdapter`
- `SMSInboundAdapter`, `SMSOutboundAdapter`

#### Collaboration Adapters
- `SlackInboundAdapter`, `SlackOutboundAdapter`
- `MicrosoftTeamsInboundAdapter`, `MicrosoftTeamsOutboundAdapter`

### Data-Access Module Classes (Complete List)

#### Entities
- `BaseEntity` - Base entity with audit fields
- `IntegrationFlow` - Integration flow definitions
- `CommunicationAdapter` - Adapter configurations
- `Message` - Message records
- `AdapterPayload` - Message payloads
- `BusinessComponent` - Multi-tenant components
- `User`, `Role` - Security entities
- `FlowStructure`, `MessageStructure` - Structure definitions
- `FieldMapping`, `TargetFieldMapping` - Field mappings
- `FlowTransformation` - Transformation definitions
- `FlowExecution` - Execution records
- `SystemLog` - System logs
- `Certificate` - SSL certificates
- `JarFile` - JAR file dependencies
- `AdapterType`, `AdapterCategory` - Adapter metadata
- `Alert`, `AlertRule` - Monitoring alerts
- `EventStore` - Event sourcing
- `SagaTransaction`, `SagaStep` - Saga pattern
- `DeadLetterMessage` - Failed messages
- `ExternalAuthentication` - OAuth configs

#### Repositories
All entities have corresponding repository interfaces in `com.integrixs.data.repository` package.

### Engine Module Classes (Complete List)

#### Core Execution
- `FlowExecutionService` - Flow execution interface
- `FlowExecutionServiceImpl` - Flow execution implementation
- `AdapterExecutor` - Adapter invocation
- `MessageRoutingService` - Message routing
- `WorkflowOrchestrationService` - Workflow orchestration

#### Transformation
- `TransformationFunction` - Transformation interface
- `TransformationFunctionExecutor` - Function execution
- `FieldMappingProcessor` - Field mapping logic
- `JsonPathTransformer` - JSON path transformations
- `HierarchicalXmlFieldMapper` - XML field mapping

#### Format Converters
- `XmlToJsonConverter` - XML to JSON
- `JsonToXmlConverter` - JSON to XML
- `XmlToCsvConverter` - XML to CSV
- `CsvToXmlConverter` - CSV to XML
- `XmlToSqlConverter` - XML to SQL
- `XmlToFixedLengthConverter` - XML to fixed-length
- `JdbcToXmlConverter` - Database to XML
- `MessageToXmlConverter` - Generic message to XML

#### Adapter Services
- `HttpAdapterService` - HTTP adapter execution
- `SoapAdapterService` - SOAP adapter execution
- `JdbcAdapterService` - Database adapter execution
- `FileAdapterService` - File adapter execution
- And implementations for all adapter types

### Monitoring Module Classes (Key Classes)
- Performance metrics collectors
- Alert evaluation services
- Monitoring dashboard services
- Health check indicators
- Audit trail management

### Shared-Lib Module Classes (Key Classes)
- Common DTOs (MessageDTO, etc.)
- Utility classes
- Shared constants and enums
- Common exception types
- Configuration models

## Quick Reference: Where to Add New Classes

| Type | Module | Package | Example |
|------|--------|---------|---------|
| REST Controller | backend | api.controller | UserController |
| Application Service | backend | application.service | UserManagementApplicationService |
| Domain Service | backend | domain.service | UserAuthenticationService |
| Entity | data-access | model | User |
| Repository | data-access | repository | UserRepository |
| Adapter Implementation | adapters | infrastructure.adapter | HttpOutboundAdapter |
| Adapter Config | adapters | config | HttpOutboundAdapterConfig |
| Transformation Function | engine | transformation | StringTransformationFunction |
| Format Converter | engine | xml | JsonToXmlConverter |
| React Component | frontend | components | UserProfileCard |
| React Page | frontend | pages | UserManagement |
| API Service | frontend | services | userService |
| Type Definition | frontend | types | User.ts |