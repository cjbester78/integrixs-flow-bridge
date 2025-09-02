# TODO Items Report

## Summary
Total TODO/FIXME items found: **5 unique TODOs** across 4 files

---

## 1. Tenant Header Support in API Client
**Files:** 
- `src/contexts/TenantContext.tsx` (lines 74, 204)

**Description:** 
Need to implement tenant header support in the API client for multi-tenant functionality.

**Context:**
```typescript
// TODO: Set tenant header for future requests when apiClient supports it
```

**Impact:** Medium - Multi-tenant support may not work correctly without proper headers
**Suggested Action:** Update apiClient to support custom headers for tenant identification

---

## 2. Authentication Context in Flow Creation
**File:** `src/hooks/useFlowActions.ts` (line 56)

**Description:**
Currently hardcoded user ID needs to be retrieved from authentication context.

**Context:**
```typescript
createdBy: 'user-integrator1' // TODO: Get from auth context
```

**Impact:** High - Incorrect user attribution for created flows
**Suggested Action:** Import and use the auth context to get the actual logged-in user ID

---

## 3. Backend Enhancement for Package Creation
**File:** `src/components/packages/PackageCreationWizard.tsx` (lines 463, 474)

**Description:**
Two backend limitations in the package creation process:
1. Backend enhancement required for some functionality (line 463)
2. Need to support multiple targets when backend is ready (line 474)

**Context:**
```typescript
// TODO: Backend Enhancement Required
// TODO: Loop through all targets when backend supports it
```

**Impact:** Low - Feature limitation, not a bug
**Suggested Action:** Coordinate with backend team to implement required APIs

---

## 4. WebSocket Filtering for Message Monitoring
**File:** `src/hooks/useMessageMonitoring.ts` (line 93)

**Description:**
WebSocket should send filtered statistics based on current filter settings.

**Context:**
```typescript
// TODO: Make WebSocket send filtered stats based on current filters
```

**Impact:** Low - Performance optimization opportunity
**Suggested Action:** Implement server-side filtering to reduce data transfer

---

## Other Findings (Not TODOs but Worth Noting)

### DEBUG References
Found several DEBUG-related items that are not TODOs:
- Logger enum values and log levels
- CSS debug comment for layout troubleshooting
- Debug logging statement

These are part of the normal codebase and don't require action.

---

## Recommendations

### Priority Order:
1. **High Priority**: Fix authentication context usage (affects data integrity)
2. **Medium Priority**: Implement tenant header support (affects multi-tenancy)
3. **Low Priority**: Backend enhancements and WebSocket filtering (feature enhancements)

### Quick Wins:
- The auth context fix can be implemented immediately
- The tenant header support could be added to the existing apiClient

### Requires Coordination:
- Backend enhancements need coordination with the backend team
- WebSocket filtering may require both frontend and backend changes