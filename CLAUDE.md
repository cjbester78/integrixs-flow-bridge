# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Integrix Flow Bridge is an enterprise integration platform built with:
- **Backend**: Java 17, Spring Boot 3.5.3, PostgreSQL
- **Frontend**: React 18, TypeScript, Vite, TanStack Query, shadcn/ui
- **Architecture**: Multi-module Maven project with microservices

This prevents misunderstandings, wasted work, and ensures alignment between user expectations and implementation.

## Standard Workflow
1. First think through the problem, read the codebase for relevant files, and write a plan to todo.md.
2. The plan should have a list of todo items that you can check off as you complete them
3. Before you begin working, check in with me and I will verify the plan.
4. Then, begin working on the todo items, marking them as complete as you go.
5. Please every step of the way just give me a high level explanation of what changes you made
6. Make every task and code change you do as simple as possible. We want to avoid making any massive or complex changes. Every change should impact as little code as possible. Everything is about simplicity.
7. Finally, add a review section to the todo.md file with a summary of the changes you made and any other relevant information.

## CRITICAL: Instruction Verification Process

**IMPORTANT**: Before implementing any user request, follow this process:

1. **Read** the user's instruction carefully
2. **Interpret** and write out your understanding of what they're asking
3. **Display** your interpretation on screen with:
   - What you think they want
   - How you plan to implement it
   - Any assumptions you're making
4. **Wait** for user confirmation that you understood correctly
5. **Only proceed** with implementation after confirmation

**CRITICAL**: never comment out code to get the application to deploy, build or start,  Fix the root error.
**CRITICAL**: Do not add hardcode variables or any hardcode values add it to a configurable value in the application.yml file.
**CRITICAL**: If it does not exist in the application.yml file it is hardcoded and this is not allowed.  Claude cannot make this decision.
**CRITICAL**: Do what has been asked; nothing more, nothing less.
**CRITICAL**: Never create work arounds.
**CRITICAL**: ALWAYS prefer editing an existing file to creating a new one.
**CRITICAL**: NEVER proactively create documentation files (*.md) or README files. Only create documentation files if explicitly requested by the User.


## Build and Run Commands

### Backend (Java/Spring Boot)

```bash
# Build entire project
mvn clean install

# Run backend only
cd backend
mvn spring-boot:run

# Run backend with specific profile
mvn spring-boot:run -Dspring.profiles.active=dev

# Run specific module tests
mvn test -pl backend
mvn test -pl adapters

# Quick startup (using script)
cd scripts
./startapp.sh

# Stop application
cd scripts
./stopapp.sh
```

### Frontend (React/Vite)

```bash
cd frontend

# Install dependencies
npm install

# Development server
npm run dev

# Build production
npm run build

# Build and copy to backend
npm run build-and-copy

# Lint
npm run lint
```

### Database

```bash
# Using Docker Compose
docker-compose up -d postgres

# Connect to PostgreSQL
psql -h localhost -p 5432 -U integrix -d integrixflowbridge
# Password: B3st3r@01
```

## Architecture Overview

### Module Structure

The project uses a multi-module Maven architecture:

- **backend**: Main Spring Boot application and REST APIs
- **adapters**: Integration adapter implementations (File, FTP, HTTP, SOAP, SAP, etc.)
- **data-access**: Data models and native SQL repositories
- **engine**: Flow execution engine
- **frontend**: React web application
- **db**: Database migrations (Flyway)
- **shared-lib**: Common utilities
- **webclient/webserver**: Web service endpoints
- **soap-bindings**: SOAP/WSDL support
- **monitoring**: System monitoring components
- **api-gateway**: API Gateway service

### Key Architectural Patterns

1. **Native XML Transformation Core**
   - All data transforms through XML as intermediate format
   - Message Structures (XSD) for non-SOAP adapters
   - Flow Structures (WSDL) for SOAP adapters
   - Visual field mapping with transformation functions

2. **Adapter Pattern**
   - AbstractAdapter base class (adapters/src/main/java/com/integrixs/adapters/core/AbstractAdapter.java)
   - Separate Inbound/Outbound handlers
   - Configuration stored as JSON in database
   - Each adapter type has dedicated configuration component

3. **Multi-tenant Architecture**
   - Business Components for logical separation
   - Tenant context propagation
   - Row-level security

4. **Event-Driven Processing**
   - Async message processing
   - Event store for audit trail
   - WebSocket for real-time updates

### Database Schema Patterns

- UUID primary keys throughout
- JSONB columns for flexible configuration
- Flyway migrations with PostgreSQL-specific features
- Soft deletes with deletion constraints
- Audit columns (created_at, updated_at, created_by, updated_by)

### Frontend Architecture

- Feature-based folder structure
- TanStack Query for server state
- Zustand for client state
- Dynamic form generation from schemas
- React Flow for visual editors

## Development Patterns

### Adding New Adapters

1. Create adapter configuration class in `adapters/src/main/java/com/integrixs/adapters/config/`
2. Implement adapter in `adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter/`
3. Add factory support in `adapters/src/main/java/com/integrixs/adapters/factory/`
4. Create frontend configuration component in `frontend/src/components/adapter/`
5. Register in `frontend/src/pages/CreateCommunicationAdapter.tsx`

### API Endpoints

- All APIs under `/api/*`
- Authentication required (JWT Bearer token)
- Standard response format with ApiResponse wrapper
- Pagination using Spring Pageable

### Database Migrations

- Migrations in `backend/src/main/resources/db/migration/`
- PostgreSQL-specific syntax
- Naming: `V{number}__{description}.sql`
- Always include rollback considerations

### Testing Approach

- Unit tests alongside source files
- Integration tests in separate module
- Use `@SpringBootTest` for integration tests
- Mock external dependencies

## Important Configuration

### Spring Profiles

- **dev**: Local development (default)
- **test**: Testing environment
- **prod**: Production environment
- **docker**: Docker deployment

### Environment Variables

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/integrixflowbridge
SPRING_DATASOURCE_USERNAME=integrix
SPRING_DATASOURCE_PASSWORD=B3st3r@01

# Security
INTEGRIX_MASTER_KEY=IntegrixFlowBridge2024SecureKey

# Email (optional)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=
SMTP_PASSWORD=
```

### Frontend Environment

```bash
# API endpoint
VITE_API_URL=http://localhost:8080

# Feature flags
VITE_FEATURE_NEW_ADAPTER_SYSTEM=false
```

## Common Tasks

### Debug Backend
```bash
# Enable debug logging
mvn spring-boot:run -Dlogging.level.com.integrixs=DEBUG

# Remote debugging
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

### Clear Database
```sql
-- Clear all transaction data
TRUNCATE TABLE messages, adapter_payloads, system_logs CASCADE;

-- Reset sequences
ALTER SEQUENCE messages_id_seq RESTART WITH 1;
```

### Generate TypeScript Types
```bash
cd frontend
# Types are manually maintained in src/types/
```

## Deployment

### Docker Deployment
```bash
# Build and run all services
docker-compose up -d

# View logs
docker-compose logs -f backend
```

### Production Build
```bash
# Backend
mvn clean package -P prod

# Frontend
cd frontend
npm run build

# Copy frontend to backend
npm run copy-to-backend
```

## Key Files to Understand

1. **Backend Architecture**
   - `backend/src/main/java/com/integrixs/backend/BackendApplication.java` - Entry point
   - `adapters/src/main/java/com/integrixs/adapters/core/AbstractAdapter.java` - Adapter base
   - `data-access/src/main/java/com/integrixs/data/model/` - Entity definitions

2. **Frontend Architecture**
   - `frontend/src/App.tsx` - Route definitions
   - `frontend/src/services/` - API service layer
   - `frontend/src/hooks/` - Custom React hooks
   - `frontend/src/components/fieldMapping/VisualMappingCanvas.tsx` - Visual mapping

3. **Configuration**
   - `backend/src/main/resources/application.yml` - Spring configuration
   - `frontend/vite.config.ts` - Frontend build configuration
   - `docker-compose.yml` - Container orchestration

## Recent Architecture Documents

- `docs/ARCHITECTURE.md` - Comprehensive module structure and class organization