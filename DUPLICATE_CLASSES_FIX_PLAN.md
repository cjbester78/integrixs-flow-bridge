# Duplicate Classes Fix Plan

## Phase 1: Critical Fixes (Week 1)
**Goal:** Eliminate compilation conflicts and package errors

### Step 1.1: Fix Backend Module Duplicates
1. **AuditService Consolidation**
   - Compare `/backend/audit/AuditService.java` vs `/backend/service/AuditService.java`
   - Determine which has more complete functionality
   - Merge unique features into one service
   - Update all references to use single service
   - Delete duplicate

2. **AuthService Consolidation**
   - Compare `/backend/auth/service/AuthService.java` vs `/backend/service/AuthService.java`
   - Likely keep the one in auth package (more appropriate location)
   - Migrate any unique functionality
   - Update references
   - Delete duplicate

### Step 1.2: Fix Shared Library Package Issue
1. **Consolidate to Single Package**
   - Choose `exceptions` (plural) as standard
   - Move all exceptions from `exception` to `exceptions`
   - Update all imports across the codebase
   - Delete empty `exception` package

### Step 1.3: Validation
- Run full compilation
- Execute all unit tests
- Verify no runtime errors

## Phase 2: Architectural Cleanup (Week 2-3)
**Goal:** Resolve cross-module duplications through proper naming

### Step 2.1: Service Naming Strategy
Apply module-specific prefixes to distinguish services:

1. **MessageRoutingService**
   - `WebClientMessageRoutingService` (webclient module)
   - `CoreMessageRoutingService` (shared-lib module) 
   - `BackendMessageRoutingService` (backend module)
   - `EngineMessageRoutingService` (engine module)

2. **AlertingService**
   - `BackendAlertingService` (backend module)
   - `MonitoringAlertingService` (monitoring module)
   - `EngineAlertingService` (engine module)

3. **AdapterExecutionService**
   - `CoreAdapterExecutionService` (shared-lib)
   - `BackendAdapterExecutionService` (backend)
   - `EngineAdapterExecutionService` (engine)

### Step 2.2: Create Service Interfaces
1. Extract common interfaces to shared-lib:
   - `IMessageRoutingService`
   - `IAlertingService`
   - `IAdapterExecutionService`

2. Have module-specific implementations extend these interfaces

### Step 2.3: Adapter Classes Organization
1. **AdapterConfiguration**
   - Rename to context-specific names:
     - `AdapterConfigProperties` (config class)
     - `AdapterConfigurationVO` (value object)
     - `AdapterConfigurationModel` (domain model)

2. **AdapterMetadata**
   - `IntegrationAdapterMetadata` (shared-lib)
   - `PluginAdapterMetadata` (plugin API)
   - `DomainAdapterMetadata` (domain model)

## Phase 3: Design Pattern Refinement (Week 4)
**Goal:** Clarify architectural patterns

### Step 3.1: Repository Pattern Clarification
1. Rename domain interfaces:
   - `UserRepositoryPort` (domain interface)
   - `UserRepository` (data-access implementation)

2. Or use package structure to distinguish:
   - `com.integrixs.backend.domain.port.UserRepository`
   - `com.integrixs.data.repository.UserRepository`

### Step 3.2: Entity vs DTO Distinction
1. Keep entity names as-is
2. Add clear DTO suffix:
   - `UserDTO`, `AlertDTO`, `FieldMappingDTO`

## Phase 4: Implementation Guidelines

### For Each Change:
1. **Before Making Changes:**
   - Read the existing code thoroughly
   - Create a comparison document
   - Identify all usages (use grep/search)
   
2. **Making Changes:**
   - Start with the duplicate that has fewer dependencies
   - Use IDE refactoring tools when possible
   - Update one module at a time
   - Maintain backward compatibility during transition

3. **Validation:**
   - Compile after each file change
   - Run relevant unit tests
   - Update integration tests if needed

### Critical Rules (per CLAUDE.md):
- **NEVER create new files unless absolutely necessary**
- **ALWAYS prefer editing existing files**
- **Make every change as simple as possible**
- **Fix all compilation errors before moving to next file**
- **Do NOT use Lombok**
- **Do NOT hardcode values - use application.yml**

## Phase 5: Testing & Validation (Ongoing)

### After Each Phase:
1. Full project compilation
2. Run all unit tests
3. Run integration tests
4. Manual testing of affected flows
5. Code review

## Rollback Plan
For each change:
1. Git commit before starting
2. Tag the commit for easy rollback
3. Document what was changed
4. Keep old class names in comments temporarily

## Success Criteria
- Zero compilation errors
- All tests passing
- No runtime ClassNotFoundException
- Clear, unambiguous class names
- Consistent naming patterns