# Package Creation Flow Redesign

## Overview
Redesign the package creation flow to offer choice between Direct Integration and Orchestration flows, with all steps contained within the package creation wizard (not separate pages).

## Current State
- Package creation uses `PackageCreationWizard` component
- Direct Integration Flow is a separate full page (`CreateDirectMappingFlow.tsx`)
- Orchestration Flow is a separate full page (`CreateOrchestrationFlow.tsx`)
- Field mapping is a reusable component (`FieldMappingScreen.tsx`)

## Target State
- All flow creation happens within the `PackageCreationWizard` modal/dialog
- User selects flow type early in the wizard
- Different wizard paths based on flow type selection
- Reuse existing field mapping component for both flow types

## Implementation Steps

### Phase 1: Add Flow Type Selection ✅
- [x] Add new wizard step 'flow-type-selection' after 'package-info'
- [x] Create FlowTypeSelection component with two options:
  - Direct Integration Flow (1 source, 1 target, 1 mapping)
  - Orchestration Flow (1 source, multiple targets, multiple mappings)
- [x] Update wizard step sequence logic to include flow type selection
- [x] Store selected flow type in wizard data

### Phase 2: Refactor Direct Integration Flow ✅
- [x] Extract core logic from `CreateDirectMappingFlow.tsx` into reusable components
- [x] Create wizard steps for Direct Integration:
  - [x] Source adapter configuration step (already existed)
  - [x] Target adapter configuration step (already existed)
  - [x] Source structure selection step (already existed)
  - [x] Target structure selection step (already existed)
  - [x] Field mapping configuration step (new)
- [x] Integrate these steps into PackageCreationWizard
- [x] Ensure field mapping works within wizard context

### Phase 3: Implement Orchestration Flow ✅
- [x] Create wizard steps for Orchestration:
  - [x] Source adapter configuration step (single)
  - [x] Target adapters configuration step (multiple)
  - [x] Source structure selection step
  - [x] Target structures selection step (one per target adapter)
  - [x] Field mappings configuration step (one per target)
- [x] Add ability to add/remove target adapters dynamically
- [x] Ensure each target has its own structure and mapping

### Phase 4: Update Package Creation Logic ✅
- [x] Modify package creation API calls to include flow type
- [x] Update component creation to handle multiple targets for orchestration
- [x] Ensure proper validation for each flow type
- [x] Add proper error handling for complex orchestration scenarios

### Phase 5: UI/UX Improvements ✅
- [x] Add visual indicators for flow type throughout wizard
- [x] Implement step validation before allowing progression
- [x] Add summary/review step showing all configurations
- [x] Ensure smooth transitions between wizard steps
- [x] Add ability to go back and modify previous selections

### Phase 6: Testing & Cleanup ✅
- [x] Test Direct Integration flow creation end-to-end
- [x] Test Orchestration flow creation with multiple targets
- [x] Verify field mappings work correctly for both flow types
- [x] Remove or deprecate standalone flow creation pages
- [x] Update navigation to remove direct flow creation routes

## Technical Considerations

### Data Structure
```typescript
interface WizardData {
  // Package info
  packageName: string;
  packageDescription: string;
  transformationRequired: boolean;
  syncType: 'SYNCHRONOUS' | 'ASYNCHRONOUS';
  
  // Flow type
  flowType: 'DIRECT_INTEGRATION' | 'ORCHESTRATION';
  
  // Direct Integration specific
  sourceAdapter?: AdapterConfig;
  targetAdapter?: AdapterConfig;
  sourceStructure?: StructureConfig;
  targetStructure?: StructureConfig;
  fieldMapping?: FieldMapping[];
  
  // Orchestration specific
  orchestrationTargets?: Array<{
    adapter: AdapterConfig;
    structure: StructureConfig;
    mapping: FieldMapping[];
  }>;
}
```

### Component Structure
```
PackageCreationWizard/
├── FlowTypeSelection.tsx
├── DirectIntegration/
│   ├── SourceAdapterStep.tsx
│   ├── TargetAdapterStep.tsx
│   ├── SourceStructureStep.tsx
│   ├── TargetStructureStep.tsx
│   └── FieldMappingStep.tsx
├── Orchestration/
│   ├── SourceConfigStep.tsx
│   ├── TargetsConfigStep.tsx
│   ├── StructuresConfigStep.tsx
│   └── MappingsConfigStep.tsx
└── ReviewStep.tsx
```

## Validation Rules

### Direct Integration Flow
- Must have exactly 1 source adapter
- Must have exactly 1 target adapter
- Must have source and target structures if transformation required
- Must have at least 1 field mapping if transformation required

### Orchestration Flow
- Must have exactly 1 source adapter
- Must have at least 1 target adapter
- Each target must have a structure if transformation required
- Each target must have at least 1 field mapping if transformation required
- Target adapters must be unique (no duplicates)

## API Changes Required
- Package creation endpoint should accept flow type parameter
- Support creating multiple target adapters in one request
- Support creating multiple field mappings in one request
- Ensure transactional creation (all or nothing)

## Migration Strategy
1. Keep existing pages functional during development
2. Add feature flag to enable new wizard flow
3. Gradually migrate users to new flow
4. Deprecate old pages once stable
5. Remove old code in future release

## Implementation Complete ✅

All phases have been successfully completed:

1. **Flow Type Selection** - Users can now choose between Direct Integration and Orchestration flows within the package creation wizard
2. **Direct Integration Flow** - Fully integrated into the wizard with all steps (adapters, structures, field mapping)
3. **Orchestration Flow** - Support for multiple target adapters with individual structures and mappings
4. **API Integration** - Package creation creates all necessary components (adapters, structures, flows, mappings)
5. **UI/UX Improvements** - Enhanced visual feedback, status indicators, and smooth transitions
6. **Cleanup** - Deprecated standalone flow creation pages with proper navigation updates

### Key Achievements:
- Unified flow creation experience through Package Creation Wizard
- Support for both Direct Integration (1-to-1) and Orchestration (1-to-many) flows
- Reusable field mapping component for both flow types
- Visual status indicators throughout the wizard
- Comprehensive review step before package creation
- Proper deprecation of old flow creation routes

### Next Steps:
- Monitor user feedback on the new unified flow
- Consider backend enhancements for full orchestration support (multiple mapping sets)
- Add more adapter type-specific configuration options
- Implement package templates for common integration patterns