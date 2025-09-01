# Data Structures Migration Plan

## Current State Analysis

### Problem
The system has partially migrated from a single `data_structures` table to specialized structure tables, but the migration is incomplete:

1. **Old Design**: Single `data_structures` table for all structure types
2. **New Design**: 
   - `message_structures` - For XML/XSD message definitions
   - `flow_structures` - For WSDL/SOAP service definitions
   - Supporting tables: `flow_structure_messages`, `flow_structure_namespaces`, `message_structure_namespaces`, etc.

### Current Issues
1. `integration_flows` table still has `source_structure_id` and `target_structure_id` referencing the old `data_structures` table
2. New columns `source_flow_structure_id` and `target_flow_structure_id` exist but only reference `flow_structures`
3. No direct reference to `message_structures` from `integration_flows`
4. Services still use the deprecated fields

## Migration Strategy

### Option 1: Complete Separation (Recommended)
Add separate fields for each structure type:
- `source_message_structure_id` → `message_structures`
- `target_message_structure_id` → `message_structures`
- `source_flow_structure_id` → `flow_structures` (already exists)
- `target_flow_structure_id` → `flow_structures` (already exists)

**Pros**: Clear separation, explicit relationships
**Cons**: More fields, need to handle which one to use

### Option 2: Polymorphic Relationship
Use generic fields with a type indicator:
- `source_structure_id` + `source_structure_type` (MESSAGE/FLOW)
- `target_structure_id` + `target_structure_type` (MESSAGE/FLOW)

**Pros**: Fewer fields, flexible
**Cons**: No foreign key constraints, more complex queries

### Option 3: Link Through Adapters
Since adapters already link to structures via `adapter_payloads`:
- Remove structure references from `integration_flows`
- Get structures through adapter → adapter_payloads → message_structures

**Pros**: Single source of truth, follows adapter configuration
**Cons**: Indirect relationship, more complex queries

## Recommended Implementation Plan (Option 1)

### Phase 1: Database Changes
1. Add new columns to `integration_flows`:
   ```sql
   ALTER TABLE integration_flows 
   ADD COLUMN source_message_structure_id UUID,
   ADD COLUMN target_message_structure_id UUID;
   
   ALTER TABLE integration_flows
   ADD CONSTRAINT fk_flows_source_message_structure 
       FOREIGN KEY (source_message_structure_id) 
       REFERENCES message_structures(id),
   ADD CONSTRAINT fk_flows_target_message_structure 
       FOREIGN KEY (target_message_structure_id) 
       REFERENCES message_structures(id);
   ```

2. Migrate existing data (if any exists in old structure table)

3. Drop old columns after migration is complete

### Phase 2: Entity Updates
1. Update `IntegrationFlow` entity:
   ```java
   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "source_message_structure_id")
   private MessageStructure sourceMessageStructure;
   
   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "target_message_structure_id")
   private MessageStructure targetMessageStructure;
   
   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "source_flow_structure_id")
   private FlowStructure sourceFlowStructure;
   
   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "target_flow_structure_id")
   private FlowStructure targetFlowStructure;
   ```

### Phase 3: Service Updates
1. Update `IntegrationFlowService` to handle structure selection based on adapter type
2. Update DTOs to include proper structure references
3. Update UI to show correct structure selection based on adapter type

### Phase 4: Data Migration
1. Analyze any existing data in `data_structures` table
2. Categorize as either message or flow structures
3. Migrate to appropriate new tables
4. Update foreign key references

### Phase 5: Cleanup
1. Remove deprecated fields from entities
2. Remove old structure handling code
3. Drop `data_structures` table

## Alternative Approach (Option 3 - Simpler)

Since `adapter_payloads` already links adapters to message structures, we could:

1. Remove all structure references from `integration_flows`
2. Rely on the adapter configuration to determine structures
3. When an adapter is selected, get its associated structures from `adapter_payloads`

This would be simpler and follows the principle that structures are tied to adapters, not flows.

## Decision Required

Which approach should we implement?
1. **Option 1**: Add explicit fields for each structure type (most explicit)
2. **Option 2**: Use polymorphic relationship (most flexible)
3. **Option 3**: Remove structure refs from flows, use adapter relationship (simplest)

## Next Steps

Once a decision is made:
1. Create database migration scripts
2. Update entity models
3. Update services and DTOs
4. Update UI components
5. Test thoroughly
6. Deploy in phases