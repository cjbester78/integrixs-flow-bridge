# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Integrix Flow Bridge is an enterprise integration middleware platform (ETL/ESB) that enables data exchange between disparate systems through visual flow composition, adapter management, and XML-based transformations.

This prevents misunderstandings, wasted work, and ensures alignment between user expectations and implementation.

## Standard Workflow
1. First think through the problem, read the codebase for relevant files, and write a plan to todo.md.
2. The plan should have a list of todo items that you can check off as you complete them
3. Before you begin working, check in with me and I will verify the plan.
4. Then, begin working on the todo items, marking them as complete as you go.
5. Please every step of the way just give me a high level explanation of what changes you made
6. Make every task and code change you do as simple as possible. We want to avoid making any massive or complex changes. Every change should impact as little code as possible. Everything is about simplicity.
7. Finally, add a review section to the todo.md file with a summary of the changes you made and any other relevant information.

**CRITICAL**: After every fix do a git commit.
**CRITICAL**: Never create workarounds or temporary fixes always implement full solution and fix root issues.
**CRITICAL**: Do what has been asked; nothing more, nothing less.
**CRITICAL**: NEVER create files unless they're absolutely necessary for achieving your goal.
**CRITICAL**: ALWAYS prefer editing an existing file to creating a new one.
**CRITICAL**: NEVER proactively create documentation files (*.md) or README files. Only create documentation files if explicitly requested by the User.

**IMPORTANT**: Before implementing any user request, follow this process:

1. **Read** the user's instruction carefully
2. **Interpret** and write out your understanding of what they're asking
3. **Display** your interpretation on screen with:
   - What you think they want
   - How you plan to implement it
   - Any assumptions you're making
4. **Wait** for user confirmation that you understood correctly
5. **Only proceed** with implementation after confirmation

## Critical Architecture Concepts

### Reversed Adapter Terminology (IMPORTANT!)
This project uses **REVERSED** middleware terminology that contradicts industry standards:
- **Sender Adapter** = Receives data FROM external systems (traditionally called "receiver/inbound")
- **Receiver Adapter** = Sends data TO external systems (traditionally called "sender/outbound")

Frontend-Backend mapping:
- **Source = Sender Adapter** (outbound operations: SELECT, polling, listening)
- **Target = Receiver Adapter** (inbound operations: INSERT, POST, sending)

### XML as Universal Format
ALL data transformations happen through XML:
1. Non-XML formats (JSON, CSV, etc.) are converted to XML at source adapter
2. Field mapping and transformations operate exclusively on XML
3. Target adapter converts from XML to required output format

### Multi-Module Maven Architecture
```
integrix-flow-bridge/
├── backend/        # Main Spring Boot app with clean architecture
├── adapters/       # 26 adapter implementations (HTTP, JDBC, FTP, etc.)
├── engine/         # Flow execution engine with Saga pattern
├── data-access/    # JPA entities & repositories
├── frontend/       # React 18 + TypeScript + Vite
└── [other modules...]
```

## Build and Development Commands

### Full Deployment (Recommended)
```bash
./deploy.sh  # Builds frontend, copies to backend, starts server
```

### Backend Development
```bash
# Build entire project
mvn clean install

# Build specific module with dependencies
mvn clean install -pl backend -am

# Run backend (from backend directory)
cd backend && mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run single test
mvn test -Dtest=ClassName#methodName

# Skip tests during build
mvn clean install -DskipTests
```

### Frontend Development
```bash
cd frontend

# Install dependencies
npm install

# Development server (hot reload)
npm run dev

# Build production
npm run build

# Build and copy to backend
npm run build-and-copy

# Lint
npm run lint
```

### Database Operations
```bash
# Connect to PostgreSQL
psql -U postgres -d integrixflowbridge

# View current migration status
SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 5;

# Manual migration (if needed)
psql -U postgres -d integrixflowbridge < backend/src/main/resources/db/migration/V[number]__[description].sql
```

## High-Level Architecture

### Flow Execution Pipeline
1. **Message Receipt**: Sender adapter receives from external system
2. **Format Conversion**: Convert to XML if needed (JSON→XML, CSV→XML)
3. **Field Mapping**: XPath-based mapping with namespace support
4. **Transformations**: Java-based custom functions
5. **Target Conversion**: XML to target format if needed
6. **Message Delivery**: Receiver adapter sends to external system

### Key Services and Their Responsibilities

**MessageQueueService** (`backend/service/`):
- Priority-based message queue with persistent storage
- Worker thread pool for parallel processing
- Automatic retry with exponential backoff

**FlowExecutionSyncService** (`backend/service/`):
- Synchronous request/response for REST/SOAP endpoints
- Correlation ID tracking
- Real-time transformation execution

**SagaTransactionService** (`backend/service/`):
- Distributed transaction coordination
- Automatic compensation on failure
- Step-by-step execution tracking

**AdapterExecutionService** (`backend/service/`):
- Adapter lifecycle management
- Connection pooling
- Health monitoring

### Security Architecture
- JWT authentication with Spring Security
- Role-based access (ADMIN, USER, VIEWER)
- Environment-based restrictions:
  - Development: Full access
  - QA: Limited write access
  - Production: Read-only + deployment

### WebSocket Real-time Updates
- `/ws/flow-execution` - Flow execution monitoring
- `/ws/messages` - Live message streaming
- `/ws/logs` - System log streaming

All WebSocket connections require JWT authentication.

## Environment Configuration

### Spring Profiles
- `dev` - Verbose logging, H2 database option
- `test` - Test configuration with mocks
- `prod` - Production optimizations

### Key Environment Variables
```bash
INTEGRIX_MASTER_KEY=<encryption key>
SPRING_PROFILES_ACTIVE=dev
DB_HOST=localhost
DB_PORT=5432
DB_NAME=integrixflowbridge
```

## API Conventions

### Base URL Structure
- All APIs prefixed with `/api`
- Frontend apiClient already includes `/api` prefix
- Don't add `/api` when using apiClient

### Authentication Headers
```javascript
Authorization: Bearer <jwt-token>
Content-Type: application/json
```

### Common API Patterns
```
GET    /api/integration-flows          # List all
GET    /api/integration-flows/{id}     # Get one
POST   /api/integration-flows          # Create
PUT    /api/integration-flows/{id}     # Update
DELETE /api/integration-flows/{id}     # Delete
POST   /api/integration-flows/{id}/deploy   # Deploy
POST   /api/integration-flows/{id}/execute  # Execute
```

## Database Schema Highlights

### Core Tables
- `integration_flows` - Flow definitions with source/target adapters
- `communication_adapters` - Adapter configurations (remember: reversed terminology!)
- `field_mappings` - XPath-based mapping rules
- `flow_transformations` - Transformation chain definitions
- `flow_executions` - Execution history and metrics
- `messages` - Message tracking with correlation IDs

### UUID Primary Keys
All tables use UUID primary keys for distributed system compatibility.

## Common Development Patterns

### Adding a New Adapter Type
1. Create config class extending `BaseAdapterConfig`
2. Implement adapter extending `AbstractSenderAdapter` or `AbstractReceiverAdapter`
3. Register in `AdapterFactory`
4. Add frontend configuration UI component

### Adding a Transformation Function
1. Add function definition to `TransformationCustomFunction` table
2. Implement in `TransformationExecutionService`
3. Add to frontend function palette

### Debugging Flow Execution
1. Check `system_logs` table for detailed logs
2. Use correlation ID to track message through pipeline
3. Monitor WebSocket `/ws/flow-execution` for real-time updates
4. Check `flow_executions` table for execution history

## Performance Considerations

### Database Optimization
- Connection pooling configured with HikariCP
- Batch operations for bulk inserts
- Proper indexes on foreign keys and search columns

### Message Processing
- Async processing with configurable thread pool
- Message priority queue for critical flows
- Circuit breaker pattern for failing adapters

### Frontend Performance
- React Query for efficient data caching
- Code splitting with dynamic imports
- WebSocket connection pooling

## Testing Strategy

### Backend Testing
```bash
# Unit tests
mvn test -pl backend

# Integration tests
mvn verify -pl integration-tests

# Specific test class
mvn test -Dtest=FlowExecutionServiceTest
```

### Frontend Testing
```bash
cd frontend
npm test          # Run all tests
npm test:watch    # Watch mode
npm test:coverage # Coverage report
```

## Deployment Notes

### Production Checklist
1. Set `SPRING_PROFILES_ACTIVE=prod`
2. Configure PostgreSQL connection
3. Set secure `INTEGRIX_MASTER_KEY`
4. Enable SSL/TLS
5. Configure monitoring endpoints
6. Set up log aggregation

### Single JAR Deployment
Frontend is bundled into backend JAR:
```bash
mvn clean package
java -jar backend/target/backend-*.jar
```

## Troubleshooting Quick Reference

### Backend Won't Start
- Check PostgreSQL connection
- Verify Flyway migrations
- Check port 8080 availability

### Frontend Build Fails
- Clear node_modules and reinstall
- Check Node.js version (18+)
- Verify frontend/backend paths in deploy.sh

### Adapter Connection Issues
- Check adapter configuration JSON
- Verify credentials in encrypted format
- Check network connectivity
- Review adapter health status

### WebSocket Disconnections
- Verify JWT token validity
- Check proxy/firewall settings
- Review WebSocket timeout configuration