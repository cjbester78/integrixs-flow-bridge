# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Integrix Flow Bridge is an enterprise integration middleware platform for visual flow composition, adapter management, and orchestration. It enables organizations to connect different systems, transform data, and orchestrate complex business processes.

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


## Architecture

The system follows a multi-module Maven architecture:

- **backend** - Spring Boot application serving REST APIs and WebSocket endpoints
- **frontend** - React/TypeScript SPA with visual flow designer
- **data-access** - JPA entities and repositories
- **adapters** - Communication protocol implementations (HTTP, FTP, SFTP, JMS, Kafka, JDBC, Mail, OData, SAP RFC/IDoc)
- **engine** - Flow execution and orchestration engine
- **shared-lib** - Common DTOs, enums, and utilities
- **webclient/webserver** - Inbound message processing endpoints
- **soap-bindings** - SOAP/WSDL support

## Common Commands

### Build and Deploy
```bash
# Full build and deploy (kills existing processes, builds frontend, starts backend)
./deploy.sh

# Build entire project
mvn clean install

# Build without tests
mvn clean install -DskipTests

# Build specific module
cd backend && mvn clean install
```

### Running Individual Components
```bash
# Run backend only (from backend directory)
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.main.allow-circular-references=true"

# Run frontend development server (from frontend directory)
npm run dev

# Build frontend for production
npm run build

# Copy frontend build to backend
npm run build-and-copy
```

### Testing
```bash
# Run all tests
mvn test

# Run tests for specific module
cd backend && mvn test

# Run integration tests
cd integration-tests && ./run-integration-tests.sh

# Frontend tests/linting
cd frontend && npm run lint
```

### Database Operations
```bash
# Create test users
psql -U integrix -d integrixflowbridge -f scripts/database/create_test_users.sql

# Clear logs and errors
./scripts/maintenance/clear_logs_and_errors.sh

# Run specific migration
mvn flyway:migrate -Dflyway.target=V123
```

## Key Architecture Patterns

### Backend Structure
The backend follows clean architecture principles with clear separation:
- **api/controller** - REST endpoints, request/response DTOs
- **application/service** - Application services orchestrating business logic
- **domain/service** - Core business logic and domain rules
- **infrastructure** - External integrations, adapters, persistence

### Authentication & Security
- JWT-based authentication with role-based access control
- Roles: ADMINISTRATOR, DEVELOPER, INTEGRATOR, VIEWER
- Multi-tenancy support with tenant isolation
- IP whitelisting and certificate management

### Integration Flow Execution
1. **Flow Definition** - Visual designer creates flow configurations
2. **Deployment** - Flows are deployed with specific adapters
3. **Execution** - Engine processes messages through transformation pipeline
4. **Monitoring** - Real-time WebSocket updates for flow execution status

### Data Model Relationships
- **BusinessComponent** → groups related integration artifacts
- **IntegrationFlow** → defines the flow logic and transformations
- **CommunicationAdapter** → configures inbound/outbound connections
- **FlowTransformation** → data transformation steps
- **FieldMapping** → visual field-to-field mappings

### Frontend Architecture
- React with TypeScript and modern hooks
- TanStack Query for server state management
- Zustand for client state
- XYFlow for visual flow editing
- shadcn/ui components with Tailwind CSS

## Important Configuration

### Environment Variables
```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=integrixflowbridge
DB_USER=integrix
DB_PASSWORD=integrix

# Security
INTEGRIX_MASTER_KEY=IntegrixFlowBridge2024SecureKey
JWT_SECRET=your-secret-key

# Environment (DEVELOPMENT, QUALITY_ASSURANCE, PRODUCTION)
SYSTEM_ENVIRONMENT_TYPE=DEVELOPMENT
```

### Spring Profiles
- **dev** - Development with debug logging
- **test** - Testing environment
- **prod** - Production with optimizations

### Key Configuration Files
- `backend/src/main/resources/application.yml` - Main Spring configuration
- `backend/src/main/resources/application-{profile}.yml` - Profile-specific configs
- `frontend/.env` - Frontend environment variables

## Development Workflow

### Adding New Adapters
1. Create adapter config class in `adapters/src/main/java/com/integrixs/adapters/config/`
2. Implement adapter in `adapters/src/main/java/com/integrixs/adapters/core/`
3. Add frontend configuration component in `frontend/src/components/adapter/`
4. Update adapter types in database

### Creating New Flows
1. Flows are created through the Package Creation Wizard (not individual flow creation)
2. Use `/packages` route to access the wizard
3. Flows support direct mapping or orchestration modes

### Database Migrations
- Migrations use Flyway in `backend/src/main/resources/db/migration/`
- Naming convention: `V{number}__{description}.sql`
- Current version: V138
- PostgreSQL-specific features are used (UUID, XML types)

## Known Issues and Workarounds

### Frontend TypeScript Issues
The frontend has TypeScript suppressions due to migration complexity. When working on frontend:
- Check for `@ts-nocheck` comments
- Review suppression files before adding new suppressions
- Prefer fixing TypeScript errors over suppressing them

### Circular Dependencies
Backend allows circular references via Spring configuration. Be cautious when adding new dependencies between modules.

### WebSocket Connections
WebSocket is used for real-time updates. Ensure proper connection handling in both frontend and backend when implementing new real-time features.

## Testing Approach

### Backend Testing
- Unit tests for services and domain logic
- Integration tests for API endpoints
- Use `@SpringBootTest` for full context tests
- Mock external dependencies with Mockito

### Frontend Testing
- Component testing with React Testing Library
- API mocking with MSW (Mock Service Worker)
- E2E tests for critical user flows

### Performance Considerations
- Database queries use batch fetching and lazy loading
- Large payloads (>1MB) are stored externally
- Connection pooling configured for high throughput
- Frontend uses virtual scrolling for large lists