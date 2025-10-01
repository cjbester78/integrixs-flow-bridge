# Integrix Flow Bridge Architecture

This document provides a comprehensive overview of the module structure and class organization for the Integrix Flow Bridge enterprise integration platform.

## Module Structure Overview

The project uses a multi-module Maven architecture with the following modules:

### Core Modules

- **backend**: Main Spring Boot application and REST APIs
- **adapters**: Integration adapter implementations (File, FTP, HTTP, SOAP, SAP, etc.)
- **data-access**: Data models and native SQL repositories
- **engine**: Flow execution engine
- **shared-lib**: Common utilities and shared DTOs

### Infrastructure Modules

- **db**: Database migrations (Flyway)
- **webclient/webserver**: Web service endpoints
- **soap-bindings**: SOAP/WSDL support
- **monitoring**: System monitoring components
- **api-gateway**: API Gateway for routing and security

### Frontend and Testing

- **frontend**: React web application
- **integration-tests**: End-to-end tests

## Key Architectural Patterns

### 1. Native SQL Data Access
- All database operations use native SQL through Spring JDBC
- JdbcTemplate for query execution
- Custom RowMappers for result set mapping
- No ORM/JPA - direct SQL control for performance

### 2. XML Transformation Core
- All data transforms through XML as intermediate format
- Message Structures (XSD) for non-SOAP adapters
- Flow Structures (WSDL) for SOAP adapters
- Visual field mapping with transformation functions

### 3. Adapter Pattern
- AbstractAdapter base class for all integrations
- Separate Inbound/Outbound handlers
- Configuration stored as JSON in database
- Factory pattern for adapter creation
- Each adapter type has dedicated configuration component

### 4. Multi-tenant Architecture
- Business Components for logical separation
- Tenant context propagation throughout the system
- Row-level security in database queries
- UUID-based tenant isolation

### 5. Event-Driven Processing
- Async message processing with Spring's @Async
- Event store for comprehensive audit trail
- WebSocket for real-time updates to frontend
- Message queuing for reliable delivery

## Database Schema Patterns

### Standard Patterns
- UUID primary keys throughout all tables
- JSONB columns for flexible configuration storage
- Flyway migrations with PostgreSQL-specific features
- Soft deletes with proper deletion constraints
- Audit columns (created_at, updated_at, created_by, updated_by)

### Key Tables
- `business_components`: Organizational units
- `communication_adapters`: Integration endpoints
- `integration_flows`: Data flow definitions
- `messages`: Message processing records
- `field_mappings`: Transformation definitions

## Module Dependencies

```
frontend
├── backend (REST APIs)
    ├── adapters (Integration logic)
    ├── engine (Flow execution)
    ├── data-access (Database operations)
    ├── monitoring (System health)
    └── shared-lib (Common utilities)
        └── db (Database migrations)
```

## Frontend Architecture

### Technology Stack
- React 18 with TypeScript
- Vite for build tooling
- TanStack Query for server state management
- Zustand for client state management
- shadcn/ui for component library
- React Flow for visual editors

### Structure
- Feature-based folder organization
- Dynamic form generation from schemas
- Real-time updates via WebSocket
- Responsive design with mobile support

## Security Architecture

### Authentication & Authorization
- JWT Bearer token authentication
- Role-based access control (RBAC)
- Multi-tenant security isolation
- API endpoint protection

### Data Security
- Credential encryption service
- Secure configuration storage
- Audit trail for all operations
- Rate limiting on API endpoints

## Performance Architecture

### Database Performance
- Connection pooling with HikariCP
- Query optimization through native SQL
- Proper indexing strategies
- Database connection monitoring

### Application Performance
- Async processing for long-running operations
- Caching strategies for frequently accessed data
- Memory management and GC tuning
- Application metrics and monitoring

## Integration Patterns

### Adapter Types
- **File Adapters**: Local file system operations
- **FTP/SFTP Adapters**: Remote file transfers
- **HTTP/REST Adapters**: Web service integration
- **SOAP Adapters**: Enterprise service integration
- **Database Adapters**: Direct database connections
- **SAP Adapters**: Enterprise system integration

### Message Flow
1. Inbound adapter receives data
2. Data transformed to internal XML format
3. Field mapping and transformation applied
4. Outbound adapter sends processed data
5. Audit trail recorded throughout

## Monitoring and Observability

### Application Monitoring
- Health checks for all adapters
- Performance metrics collection
- Error tracking and alerting
- Resource utilization monitoring

### Business Monitoring
- Message processing statistics
- Integration flow success rates
- Adapter availability tracking
- Business component metrics

## Development Guidelines

### Code Organization
- Package by feature, not by layer
- Clear separation of concerns
- Dependency injection throughout
- Comprehensive error handling

### Testing Strategy
- Unit tests for business logic
- Integration tests for database operations
- End-to-end tests for complete flows
- Mock external dependencies

### Configuration Management
- Environment-specific configurations
- Externalized configuration values
- No hardcoded values in source code
- Configuration validation on startup

## Deployment Architecture

### Container Strategy
- Docker containers for all components
- Docker Compose for local development
- Production deployment with orchestration
- Health checks and auto-recovery

### Database Strategy
- PostgreSQL for primary data storage
- Connection pooling and failover
- Backup and recovery procedures
- Migration management with Flyway

This architecture supports enterprise-grade integration requirements while maintaining flexibility for future enhancements and scalability needs.