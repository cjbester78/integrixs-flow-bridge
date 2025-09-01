# Adapter Naming Refactoring Plan - Industry Standard Compliance

## Overview
This document outlines the comprehensive plan to refactor the adapter naming convention from the current reversed terminology to industry-standard naming.

**Current (Reversed):**
- Sender Adapter = Receives data FROM external systems (inbound)
- Receiver Adapter = Sends data TO external systems (outbound)

**Target (Industry Standard):**
- Inbound Adapter = Receives data FROM external systems
- Outbound Adapter = Sends data TO external systems

## Phase 1: Preparation and Analysis

### 1.1 Create Comprehensive Test Suite
- [ ] Document current adapter behavior
- [ ] Create integration tests for all adapter types
- [ ] Ensure 100% test coverage for critical paths
- [ ] Create performance benchmarks

### 1.2 Database Backup and Analysis
- [ ] Full database backup
- [ ] Analyze all tables with adapter references
- [ ] Document current data state
- [ ] Create rollback scripts

### 1.3 Code Impact Analysis
- [ ] List all Java classes to rename
- [ ] List all configuration classes
- [ ] List all frontend files affected
- [ ] Identify API endpoints affected

## Phase 2: Backend Refactoring

### 2.1 Core Interface Changes
```
SenderAdapterPort → InboundAdapterPort
ReceiverAdapterPort → OutboundAdapterPort
AbstractSenderAdapter → AbstractInboundAdapter
AbstractReceiverAdapter → AbstractOutboundAdapter
```

### 2.2 Adapter Implementation Classes
```
# File-based Adapters
FileSenderAdapter → FileInboundAdapter
FileReceiverAdapter → FileOutboundAdapter
FtpSenderAdapter → FtpInboundAdapter
FtpReceiverAdapter → FtpOutboundAdapter
SftpSenderAdapter → SftpInboundAdapter
SftpReceiverAdapter → SftpOutboundAdapter

# Database Adapters
JdbcSenderAdapter → JdbcInboundAdapter
JdbcReceiverAdapter → JdbcOutboundAdapter

# Messaging Adapters
JmsSenderAdapter → JmsInboundAdapter
JmsReceiverAdapter → JmsOutboundAdapter
KafkaSenderAdapter → KafkaInboundAdapter
KafkaReceiverAdapter → KafkaOutboundAdapter

# Web Service Adapters
HttpSenderAdapter → HttpInboundAdapter
HttpReceiverAdapter → HttpOutboundAdapter
RestSenderAdapter → RestInboundAdapter
RestReceiverAdapter → RestOutboundAdapter
SoapSenderAdapter → SoapInboundAdapter
SoapReceiverAdapter → SoapOutboundAdapter

# Email Adapters
MailSenderAdapter → MailInboundAdapter
MailReceiverAdapter → MailOutboundAdapter

# SAP Adapters
RfcSenderAdapter → RfcInboundAdapter
RfcReceiverAdapter → RfcOutboundAdapter
IdocSenderAdapter → IdocInboundAdapter
IdocReceiverAdapter → IdocOutboundAdapter

# OData Adapters
OdataSenderAdapter → OdataInboundAdapter
OdataReceiverAdapter → OdataOutboundAdapter
```

### 2.3 Configuration Classes
```
*SenderAdapterConfig → *InboundAdapterConfig
*ReceiverAdapterConfig → *OutboundAdapterConfig
```

### 2.4 Enum Updates
```java
public enum AdapterModeEnum {
    INBOUND,  // was SENDER
    OUTBOUND  // was RECEIVER
}
```

### 2.5 Package Structure (Optional)
Consider reorganizing:
```
com.integrixs.adapters.infrastructure.adapter
├── inbound/
│   ├── FileInboundAdapter.java
│   ├── FtpInboundAdapter.java
│   └── ...
└── outbound/
    ├── FileOutboundAdapter.java
    ├── FtpOutboundAdapter.java
    └── ...
```

## Phase 3: Database Migration

### 3.1 Schema Updates
```sql
-- Update adapter mode enum
ALTER TYPE adapter_mode_enum RENAME VALUE 'SENDER' TO 'INBOUND';
ALTER TYPE adapter_mode_enum RENAME VALUE 'RECEIVER' TO 'OUTBOUND';

-- Update flow structure direction enum if exists
UPDATE flow_structures 
SET direction = CASE 
    WHEN direction = 'SOURCE' THEN 'INBOUND'
    WHEN direction = 'TARGET' THEN 'OUTBOUND'
    ELSE direction
END;
```

### 3.2 Data Migration
```sql
-- Update communication adapters
UPDATE communication_adapters 
SET mode = CASE 
    WHEN mode = 'SENDER' THEN 'INBOUND'
    WHEN mode = 'RECEIVER' THEN 'OUTBOUND'
END;

-- Update any JSON configurations that might contain mode
UPDATE communication_adapters
SET configuration = 
    REPLACE(
        REPLACE(configuration::text, '"mode":"SENDER"', '"mode":"INBOUND"'),
        '"mode":"RECEIVER"', '"mode":"OUTBOUND"'
    )::json
WHERE configuration::text LIKE '%"mode"%';
```

### 3.3 Flow Mapping Update
```sql
-- Rename columns for clarity
ALTER TABLE integration_flows 
    RENAME COLUMN source_adapter_id TO inbound_adapter_id;
ALTER TABLE integration_flows 
    RENAME COLUMN target_adapter_id TO outbound_adapter_id;
```

## Phase 4: Frontend Refactoring

### 4.1 TypeScript Type Updates
```typescript
// Old
export enum AdapterMode {
  SENDER = "SENDER",
  RECEIVER = "RECEIVER"
}

// New
export enum AdapterMode {
  INBOUND = "INBOUND",
  OUTBOUND = "OUTBOUND"
}
```

### 4.2 Component Updates
- [ ] Update all adapter configuration components
- [ ] Update flow designer components
- [ ] Update adapter selection dropdowns
- [ ] Update validation logic

### 4.3 Language/Labels
- [ ] Update all UI labels
- [ ] Update tooltips and help text
- [ ] Update error messages
- [ ] Update documentation strings

### 4.4 API Integration Layer
```typescript
// Update API service calls
const createAdapter = (config: AdapterConfig) => {
  // Map old terminology if needed for backwards compatibility
  const mode = config.mode === 'SENDER' ? 'INBOUND' : 
               config.mode === 'RECEIVER' ? 'OUTBOUND' : config.mode;
  // ...
}
```

## Phase 5: API Updates

### 5.1 Versioning Strategy
- Keep v1 API with deprecation notice
- Create v2 API with new terminology
- Add migration period (3-6 months)

### 5.2 Backward Compatibility Layer
```java
@RestController
@RequestMapping("/api/v1/adapters")
@Deprecated
public class LegacyAdapterController {
    // Map old terminology to new
}

@RestController
@RequestMapping("/api/v2/adapters")
public class AdapterController {
    // Use new terminology
}
```

## Phase 6: Documentation

### 6.1 Code Documentation
- [ ] Remove all "reversed middleware convention" comments
- [ ] Update JavaDoc comments
- [ ] Update inline documentation
- [ ] Update README files

### 6.2 Architecture Documentation
- [ ] Update architecture diagrams
- [ ] Update API documentation
- [ ] Update user guides
- [ ] Create migration guide

## Phase 7: Testing and Validation

### 7.1 Automated Testing
- [ ] Run all unit tests
- [ ] Run integration tests
- [ ] Run performance tests
- [ ] Validate data migration

### 7.2 Manual Testing
- [ ] Test each adapter type
- [ ] Test flow creation/editing
- [ ] Test existing flows
- [ ] Test API backwards compatibility

## Phase 8: Deployment

### 8.1 Deployment Strategy
1. Deploy to development environment
2. Run smoke tests
3. Deploy to staging environment
4. User acceptance testing
5. Production deployment with rollback plan

### 8.2 Rollback Plan
- Database backup restoration script
- Code rollback procedure
- Communication plan

## Refactoring Scripts

### Backend Rename Script
```bash
#!/bin/bash
# rename_adapters.sh

# Create backup
cp -r adapters adapters.backup

# Rename files
find adapters -name "*SenderAdapter.java" | while read f; do 
    mv "$f" "${f/SenderAdapter/InboundAdapter}"
done

find adapters -name "*ReceiverAdapter.java" | while read f; do 
    mv "$f" "${f/ReceiverAdapter/OutboundAdapter}"
done

# Update file contents
find adapters -type f -name "*.java" -exec sed -i.bak \
    -e 's/SenderAdapter/InboundAdapter/g' \
    -e 's/ReceiverAdapter/OutboundAdapter/g' \
    -e 's/SenderAdapterPort/InboundAdapterPort/g' \
    -e 's/ReceiverAdapterPort/OutboundAdapterPort/g' \
    -e 's/SENDER/INBOUND/g' \
    -e 's/RECEIVER/OUTBOUND/g' \
    {} +
```

### Frontend Rename Script
```bash
#!/bin/bash
# rename_frontend.sh

# TypeScript/React files
find frontend/src -type f \( -name "*.ts" -o -name "*.tsx" \) -exec sed -i.bak \
    -e 's/sender/inbound/g' \
    -e 's/Sender/Inbound/g' \
    -e 's/SENDER/INBOUND/g' \
    -e 's/receiver/outbound/g' \
    -e 's/Receiver/Outbound/g' \
    -e 's/RECEIVER/OUTBOUND/g' \
    -e 's/source/inbound/g' \
    -e 's/Source/Inbound/g' \
    -e 's/target/outbound/g' \
    -e 's/Target/Outbound/g' \
    {} +
```

## Risk Mitigation

### High-Risk Areas
1. **Data Migration** - Test thoroughly in staging
2. **Active Flows** - May need to pause during migration
3. **External Integrations** - Notify all consumers
4. **User Training** - Prepare documentation

### Mitigation Strategies
1. **Feature Flags** - Deploy with ability to toggle
2. **Canary Deployment** - Roll out gradually
3. **Monitoring** - Enhanced monitoring during migration
4. **Communication** - Clear user communication

## Timeline

### Week 1: Preparation
- Day 1-2: Test suite creation
- Day 3: Database analysis and backup
- Day 4-5: Impact analysis and planning

### Week 2: Backend Implementation
- Day 1-2: Core interfaces and abstracts
- Day 3-4: Adapter implementations
- Day 5: Database migration scripts

### Week 3: Frontend & Testing
- Day 1-2: Frontend refactoring
- Day 3: API updates
- Day 4-5: Testing and bug fixes

### Week 4: Documentation & Deployment
- Day 1-2: Documentation updates
- Day 3: Staging deployment
- Day 4-5: Production deployment

## Success Criteria

1. All adapters use industry-standard naming
2. No breaking changes for existing flows
3. All tests pass
4. Documentation is updated
5. Users can create new flows with new terminology
6. API v1 still works with deprecation notices

## Glossary

| Old Term | New Term | Description |
|----------|----------|-------------|
| Sender Adapter | Inbound Adapter | Receives data FROM external systems |
| Receiver Adapter | Outbound Adapter | Sends data TO external systems |
| Source | Inbound | Where data comes from |
| Target | Outbound | Where data goes to |
| SENDER mode | INBOUND mode | Adapter mode for receiving data |
| RECEIVER mode | OUTBOUND mode | Adapter mode for sending data |