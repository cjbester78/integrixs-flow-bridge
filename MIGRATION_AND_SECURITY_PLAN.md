# Migration and Security Plan for Data Structures

## Current Workflow Understanding

The system follows this hierarchy:
1. **Message Structure** (XML/XSD definitions)
2. **Flow Structure** (WSDL/SOAP) → references Message Structures
3. **Communication Adapter** → references Flow Structures
4. **Integration Flow** → references both Adapters AND Flow Structures
5. **Field Mappings** → based on the structures

## Part 1: Complete the Migration

### The Issue
- `integration_flows` still uses deprecated `source_structure_id` and `target_structure_id` (pointing to old `data_structures`)
- New fields `source_flow_structure_id` and `target_flow_structure_id` exist but aren't properly used
- Services still populate the old fields

### Migration Steps

#### Step 1: Update the Service Layer
Update `FlowCompositionService` to use the new fields:

```java
// Instead of:
flow.setSourceStructureId(UUID.fromString(request.getSourceStructureId()));
flow.setTargetStructureId(UUID.fromString(request.getTargetStructureId()));

// Use:
flow.setSourceFlowStructureId(UUID.fromString(request.getSourceFlowStructureId()));
flow.setTargetFlowStructureId(UUID.fromString(request.getTargetFlowStructureId()));
```

#### Step 2: Update DTOs
Ensure DTOs use `sourceFlowStructureId` and `targetFlowStructureId` instead of the old fields.

#### Step 3: Data Migration Script
```sql
-- Migrate any existing data from old columns to new ones
UPDATE integration_flows 
SET source_flow_structure_id = source_structure_id 
WHERE source_structure_id IS NOT NULL 
  AND source_flow_structure_id IS NULL;

UPDATE integration_flows 
SET target_flow_structure_id = target_structure_id 
WHERE target_structure_id IS NOT NULL 
  AND target_flow_structure_id IS NULL;
```

#### Step 4: Remove Deprecated Fields
After ensuring all data is migrated and code updated:
```sql
ALTER TABLE integration_flows 
DROP COLUMN source_structure_id,
DROP COLUMN target_structure_id;
```

## Part 2: Security - Prevent Deletion of Referenced Entities

### Current Problem
Entities can be deleted even when other entities depend on them, causing data integrity issues.

### Solution: Add Proper Foreign Key Constraints

#### 1. Message Structures Protection
```sql
-- Prevent deletion of message_structures referenced by flow_structure_messages
ALTER TABLE flow_structure_messages
DROP CONSTRAINT IF EXISTS fk_flow_structure_messages_message,
ADD CONSTRAINT fk_flow_structure_messages_message 
    FOREIGN KEY (message_structure_id) 
    REFERENCES message_structures(id) 
    ON DELETE RESTRICT;
```

#### 2. Flow Structures Protection
```sql
-- Prevent deletion of flow_structures referenced by adapters
ALTER TABLE adapter_payloads
DROP CONSTRAINT IF EXISTS fk_adapter_payloads_flow_structure,
ADD CONSTRAINT fk_adapter_payloads_flow_structure 
    FOREIGN KEY (flow_structure_id) 
    REFERENCES flow_structures(id) 
    ON DELETE RESTRICT;

-- Prevent deletion of flow_structures referenced by integration_flows
ALTER TABLE integration_flows
DROP CONSTRAINT IF EXISTS fk_flows_source_flow_structure,
ADD CONSTRAINT fk_flows_source_flow_structure 
    FOREIGN KEY (source_flow_structure_id) 
    REFERENCES flow_structures(id) 
    ON DELETE RESTRICT;

ALTER TABLE integration_flows
DROP CONSTRAINT IF EXISTS fk_flows_target_flow_structure,
ADD CONSTRAINT fk_flows_target_flow_structure 
    FOREIGN KEY (target_flow_structure_id) 
    REFERENCES flow_structures(id) 
    ON DELETE RESTRICT;
```

#### 3. Adapters Protection
```sql
-- Already exists but ensure it's RESTRICT not CASCADE
ALTER TABLE integration_flows
DROP CONSTRAINT IF EXISTS fk_flows_source_adapter,
ADD CONSTRAINT fk_flows_source_adapter 
    FOREIGN KEY (source_adapter_id) 
    REFERENCES communication_adapters(id) 
    ON DELETE RESTRICT;

ALTER TABLE integration_flows
DROP CONSTRAINT IF EXISTS fk_flows_target_adapter,
ADD CONSTRAINT fk_flows_target_adapter 
    FOREIGN KEY (target_adapter_id) 
    REFERENCES communication_adapters(id) 
    ON DELETE RESTRICT;
```

#### 4. Integration Flows Protection
```sql
-- Prevent deletion of flows with executions
ALTER TABLE flow_executions
DROP CONSTRAINT IF EXISTS fk_executions_flow,
ADD CONSTRAINT fk_executions_flow 
    FOREIGN KEY (flow_id) 
    REFERENCES integration_flows(id) 
    ON DELETE RESTRICT;

-- Prevent deletion of flows with transformations
ALTER TABLE flow_transformations
DROP CONSTRAINT IF EXISTS fk_transformations_flow,
ADD CONSTRAINT fk_transformations_flow 
    FOREIGN KEY (flow_id) 
    REFERENCES integration_flows(id) 
    ON DELETE RESTRICT;
```

### Service Layer Enhancements

Add validation in services before deletion:

```java
@Service
public class MessageStructureService {
    
    public void deleteMessageStructure(UUID id) {
        // Check if referenced by flow_structure_messages
        if (flowStructureMessageRepository.existsByMessageStructureId(id)) {
            throw new IllegalStateException(
                "Cannot delete message structure: It is referenced by flow structures"
            );
        }
        messageStructureRepository.deleteById(id);
    }
}

@Service
public class FlowStructureService {
    
    public void deleteFlowStructure(UUID id) {
        // Check if referenced by adapters
        if (adapterPayloadRepository.existsByFlowStructureId(id)) {
            throw new IllegalStateException(
                "Cannot delete flow structure: It is referenced by adapters"
            );
        }
        // Check if referenced by integration flows
        if (integrationFlowRepository.existsBySourceFlowStructureIdOrTargetFlowStructureId(id, id)) {
            throw new IllegalStateException(
                "Cannot delete flow structure: It is referenced by integration flows"
            );
        }
        flowStructureRepository.deleteById(id);
    }
}
```

## Implementation Order

1. **First**: Add all foreign key constraints with ON DELETE RESTRICT
2. **Second**: Update service layer validation
3. **Third**: Migrate data from old structure fields to new ones
4. **Fourth**: Update all code to use new fields
5. **Finally**: Drop deprecated columns

## Testing Plan

1. Create a message structure
2. Create a flow structure linking to it
3. Create an adapter linking to the flow structure
4. Create an integration flow using the adapter and flow structure
5. Try to delete each entity in reverse order - should fail with proper error messages
6. Delete in correct order (flow → adapter → flow structure → message structure) - should succeed

## Benefits

1. **Data Integrity**: Prevents orphaned references
2. **Clear Error Messages**: Users understand why deletion failed
3. **Proper Workflow**: Enforces correct deletion order
4. **Audit Trail**: Can track what depends on what

## Database Migration Script

```sql
-- V122__add_deletion_security_constraints.sql

-- Message Structures Protection
ALTER TABLE flow_structure_messages
DROP CONSTRAINT IF EXISTS fk_flow_structure_messages_message,
ADD CONSTRAINT fk_flow_structure_messages_message 
    FOREIGN KEY (message_structure_id) 
    REFERENCES message_structures(id) 
    ON DELETE RESTRICT;

-- Flow Structures Protection  
ALTER TABLE adapter_payloads
DROP CONSTRAINT IF EXISTS fk_adapter_payloads_flow_structure,
ADD CONSTRAINT fk_adapter_payloads_flow_structure 
    FOREIGN KEY (flow_structure_id) 
    REFERENCES flow_structures(id) 
    ON DELETE RESTRICT;

ALTER TABLE integration_flows
DROP CONSTRAINT IF EXISTS fk_flows_source_flow_structure,
ADD CONSTRAINT fk_flows_source_flow_structure 
    FOREIGN KEY (source_flow_structure_id) 
    REFERENCES flow_structures(id) 
    ON DELETE RESTRICT;

ALTER TABLE integration_flows
DROP CONSTRAINT IF EXISTS fk_flows_target_flow_structure,
ADD CONSTRAINT fk_flows_target_flow_structure 
    FOREIGN KEY (target_flow_structure_id) 
    REFERENCES flow_structures(id) 
    ON DELETE RESTRICT;

-- Adapters Protection
ALTER TABLE integration_flows
DROP CONSTRAINT IF EXISTS fk_flows_source_adapter,
ADD CONSTRAINT fk_flows_source_adapter 
    FOREIGN KEY (source_adapter_id) 
    REFERENCES communication_adapters(id) 
    ON DELETE RESTRICT;

ALTER TABLE integration_flows
DROP CONSTRAINT IF EXISTS fk_flows_target_adapter,
ADD CONSTRAINT fk_flows_target_adapter 
    FOREIGN KEY (target_adapter_id) 
    REFERENCES communication_adapters(id) 
    ON DELETE RESTRICT;

-- Integration Flows Protection
ALTER TABLE flow_executions
DROP CONSTRAINT IF EXISTS fk_executions_flow,
ADD CONSTRAINT fk_executions_flow 
    FOREIGN KEY (flow_id) 
    REFERENCES integration_flows(id) 
    ON DELETE RESTRICT;

ALTER TABLE flow_transformations
DROP CONSTRAINT IF EXISTS fk_transformations_flow,
ADD CONSTRAINT fk_transformations_flow 
    FOREIGN KEY (flow_id) 
    REFERENCES integration_flows(id) 
    ON DELETE RESTRICT;

ALTER TABLE field_mappings
DROP CONSTRAINT IF EXISTS fk_mappings_transformation,
ADD CONSTRAINT fk_mappings_transformation 
    FOREIGN KEY (transformation_id) 
    REFERENCES flow_transformations(id) 
    ON DELETE RESTRICT;
```