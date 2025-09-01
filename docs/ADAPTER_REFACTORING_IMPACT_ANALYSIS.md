# Adapter Refactoring Impact Analysis

## Overview
This document provides a comprehensive analysis of all files and code sections that need to be updated during the adapter naming refactoring.

## File Renaming Requirements

### Java Files to Rename (59 files)

#### Configuration Classes (26 files)
```
adapters/src/main/java/com/integrixs/adapters/config/
├── FileSenderAdapterConfig.java → FileInboundAdapterConfig.java
├── FileReceiverAdapterConfig.java → FileOutboundAdapterConfig.java
├── FtpSenderAdapterConfig.java → FtpInboundAdapterConfig.java
├── FtpReceiverAdapterConfig.java → FtpOutboundAdapterConfig.java
├── SftpSenderAdapterConfig.java → SftpInboundAdapterConfig.java
├── SftpReceiverAdapterConfig.java → SftpOutboundAdapterConfig.java
├── JdbcSenderAdapterConfig.java → JdbcInboundAdapterConfig.java
├── JdbcReceiverAdapterConfig.java → JdbcOutboundAdapterConfig.java
├── JmsSenderAdapterConfig.java → JmsInboundAdapterConfig.java
├── JmsReceiverAdapterConfig.java → JmsOutboundAdapterConfig.java
├── KafkaSenderAdapterConfig.java → KafkaInboundAdapterConfig.java
├── KafkaReceiverAdapterConfig.java → KafkaOutboundAdapterConfig.java
├── HttpSenderAdapterConfig.java → HttpInboundAdapterConfig.java
├── HttpReceiverAdapterConfig.java → HttpOutboundAdapterConfig.java
├── RestSenderAdapterConfig.java → RestInboundAdapterConfig.java
├── RestReceiverAdapterConfig.java → RestOutboundAdapterConfig.java
├── SoapSenderAdapterConfig.java → SoapInboundAdapterConfig.java
├── SoapReceiverAdapterConfig.java → SoapOutboundAdapterConfig.java
├── MailSenderAdapterConfig.java → MailInboundAdapterConfig.java
├── MailReceiverAdapterConfig.java → MailOutboundAdapterConfig.java
├── RfcSenderAdapterConfig.java → RfcInboundAdapterConfig.java
├── RfcReceiverAdapterConfig.java → RfcOutboundAdapterConfig.java
├── IdocSenderAdapterConfig.java → IdocInboundAdapterConfig.java
├── IdocReceiverAdapterConfig.java → IdocOutboundAdapterConfig.java
├── OdataSenderAdapterConfig.java → OdataInboundAdapterConfig.java
└── OdataReceiverAdapterConfig.java → OdataOutboundAdapterConfig.java
```

#### Core/Domain Classes (6 files)
```
adapters/src/main/java/com/integrixs/adapters/
├── core/
│   ├── AbstractSenderAdapter.java → AbstractInboundAdapter.java
│   ├── AbstractReceiverAdapter.java → AbstractOutboundAdapter.java
│   ├── SenderAdapter.java → InboundAdapter.java
│   └── ReceiverAdapter.java → OutboundAdapter.java
└── domain/port/
    ├── SenderAdapterPort.java → InboundAdapterPort.java
    └── ReceiverAdapterPort.java → OutboundAdapterPort.java
```

#### Implementation Classes (26 files)
```
adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter/
├── FileSenderAdapter.java → FileInboundAdapter.java
├── FileReceiverAdapter.java → FileOutboundAdapter.java
├── FtpSenderAdapter.java → FtpInboundAdapter.java
├── FtpReceiverAdapter.java → FtpOutboundAdapter.java
├── SftpSenderAdapter.java → SftpInboundAdapter.java
├── SftpReceiverAdapter.java → SftpOutboundAdapter.java
├── JdbcSenderAdapter.java → JdbcInboundAdapter.java
├── JdbcReceiverAdapter.java → JdbcOutboundAdapter.java
├── JmsSenderAdapter.java → JmsInboundAdapter.java
├── JmsReceiverAdapter.java → JmsOutboundAdapter.java
├── KafkaSenderAdapter.java → KafkaInboundAdapter.java
├── KafkaReceiverAdapter.java → KafkaOutboundAdapter.java
├── HttpSenderAdapter.java → HttpInboundAdapter.java
├── HttpReceiverAdapter.java → HttpOutboundAdapter.java
├── RestSenderAdapter.java → RestInboundAdapter.java
├── RestReceiverAdapter.java → RestOutboundAdapter.java
├── SoapSenderAdapter.java → SoapInboundAdapter.java
├── SoapReceiverAdapter.java → SoapOutboundAdapter.java
├── MailSenderAdapter.java → MailInboundAdapter.java
├── MailReceiverAdapter.java → MailOutboundAdapter.java
├── RfcSenderAdapter.java → RfcInboundAdapter.java
├── RfcReceiverAdapter.java → RfcOutboundAdapter.java
├── IdocSenderAdapter.java → IdocInboundAdapter.java
├── IdocReceiverAdapter.java → IdocOutboundAdapter.java
├── OdataSenderAdapter.java → OdataInboundAdapter.java
└── OdataReceiverAdapter.java → OdataOutboundAdapter.java
```

#### Test Classes (1 file found, more needed)
```
adapters/src/test/java/com/integrixs/adapters/infrastructure/adapter/
└── JdbcSenderAdapterTest.java → JdbcInboundAdapterTest.java
```

## Code Changes Required

### 1. Enum Updates
Files containing AdapterModeEnum:
- `adapters/src/main/java/com/integrixs/adapters/domain/model/AdapterConfiguration.java`
  - Change: `SENDER` → `INBOUND`
  - Change: `RECEIVER` → `OUTBOUND`

### 2. Import Statement Updates
All files importing adapter classes need updates:
- Factory classes
- Service classes
- Controller classes
- Test classes

### 3. Class References
Files that instantiate or reference adapter classes:
- AdapterFactory implementations
- AdapterService implementations
- AdapterController classes
- Integration test classes

### 4. String Literals
Search and replace in all Java files:
- `"SENDER"` → `"INBOUND"`
- `"RECEIVER"` → `"OUTBOUND"`
- `"sender"` → `"inbound"`
- `"receiver"` → `"outbound"`

### 5. Comments and Documentation
Update all JavaDoc comments and inline comments:
- References to "Sender adapter"
- References to "Receiver adapter"
- References to "middleware convention"

## Backend Files Requiring Content Updates (Estimated)

### Service Layer
- `backend/src/main/java/com/integrixs/backend/service/adapter/`
  - AdapterService.java
  - AdapterFactory.java
  - AdapterRegistry.java
  - AdapterConfigurationService.java

### Controller Layer
- `backend/src/main/java/com/integrixs/backend/controller/`
  - AdapterController.java
  - FlowController.java
  - IntegrationController.java

### Domain/Model Layer
- `backend/src/main/java/com/integrixs/backend/domain/`
  - CommunicationAdapter.java
  - IntegrationFlow.java
  - FlowStructure.java

### Repository Layer
- `backend/src/main/java/com/integrixs/backend/repository/`
  - AdapterRepository.java
  - FlowRepository.java

## Frontend Impact Analysis

### TypeScript Types/Interfaces
Files to check:
- `frontend/src/types/adapter.types.ts`
- `frontend/src/types/flow.types.ts`

### API Service Files
- `frontend/src/services/adapterService.ts`
- `frontend/src/services/flowService.ts`

### Component Files
- `frontend/src/components/adapters/`
- `frontend/src/components/flows/`

### Constants/Enums
- `frontend/src/constants/adapterConstants.ts`

## Database References

### Tables with Column Changes
1. `integration_flows`
   - `source_adapter_id` → `inbound_adapter_id`
   - `target_adapter_id` → `outbound_adapter_id`

2. `communication_adapters`
   - `mode` enum values: SENDER → INBOUND, RECEIVER → OUTBOUND

3. `flow_structures`
   - `direction` values: SOURCE → INBOUND, TARGET → OUTBOUND

### JSON Configuration Updates
- All `configuration` JSON columns containing mode references

## API Endpoint Changes

### Existing Endpoints (v1 - maintain for compatibility)
- `GET /api/v1/adapters?mode=SENDER`
- `GET /api/v1/adapters?mode=RECEIVER`
- `POST /api/v1/adapters` (body contains mode)

### New Endpoints (v2)
- `GET /api/v2/adapters?mode=INBOUND`
- `GET /api/v2/adapters?mode=OUTBOUND`
- `POST /api/v2/adapters` (body contains mode)

## Configuration Files

### Application Properties
Check for any references in:
- `application.yml`
- `application.properties`
- `application-*.yml`

### Docker/Deployment Configs
- Docker Compose files
- Kubernetes manifests
- CI/CD pipelines

## Testing Requirements

### Unit Tests to Create/Update
- One test class per adapter implementation
- Factory tests
- Service layer tests
- Controller tests

### Integration Tests
- End-to-end flow tests
- API compatibility tests
- Database migration tests

## Estimated Effort

### File Renaming
- 59 Java files to rename
- Automated with script: 1 hour

### Code Updates
- 84+ Java files with references
- Manual review required: 8-16 hours

### Frontend Updates
- Type definitions and API calls
- Estimated: 4-8 hours

### Database Migration
- Schema changes and data migration
- Testing required: 4-8 hours

### Testing
- Write new tests: 16-24 hours
- Run existing tests: 2-4 hours
- Fix failing tests: 8-16 hours

### Documentation
- Update all docs: 4-8 hours

### Total Estimated Effort
- Development: 40-60 hours
- Testing: 20-30 hours
- Documentation: 10-15 hours
- **Total: 70-105 hours (2-3 weeks)**

## Risk Areas

1. **Active Production Flows**: Need careful migration strategy
2. **External Integrations**: May depend on current API structure
3. **Configuration Management**: JSON configs in database
4. **Third-party Dependencies**: May expect certain naming
5. **Backward Compatibility**: Critical for existing deployments

## Rollback Plan

1. Database backup before migration
2. Git branch for all code changes
3. Ability to deploy old version
4. Database rollback script ready
5. API v1 maintained for compatibility

---

This analysis should be reviewed and updated as the refactoring progresses.