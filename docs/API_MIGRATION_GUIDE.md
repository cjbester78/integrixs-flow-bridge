# API Migration Guide: Adapter Terminology Update

## Overview
This guide helps API consumers migrate from the legacy adapter terminology (SENDER/RECEIVER) to the industry-standard terminology (INBOUND/OUTBOUND).

## API Versioning Strategy

### Current State
- **API v1** (Deprecated): `/api/v1/communication-adapters` - Uses SENDER/RECEIVER
- **API v2** (Recommended): `/api/v2/communication-adapters` - Uses INBOUND/OUTBOUND
- **Default** (Legacy): `/api/communication-adapters` - Currently uses mixed terminology

### Migration Timeline
- **Now**: Both v1 and v2 APIs are available
- **3 months**: Deprecation warnings added to v1 responses
- **6 months**: v1 API marked for removal
- **12 months**: v1 API removed, only v2 available

## Terminology Mapping

| Old Term (v1) | New Term (v2) | Description |
|---------------|---------------|-------------|
| SENDER | INBOUND | Adapter that receives data FROM external systems |
| RECEIVER | OUTBOUND | Adapter that sends data TO external systems |
| source_adapter_id | inbound_adapter_id | Database column for flow's inbound adapter |
| target_adapter_id | outbound_adapter_id | Database column for flow's outbound adapter |

## API Endpoint Changes

### 1. Get All Adapters

**v1 (Deprecated)**
```http
GET /api/v1/communication-adapters?mode=SENDER
GET /api/v1/communication-adapters?mode=RECEIVER
```

**v2 (Recommended)**
```http
GET /api/v2/communication-adapters?mode=INBOUND
GET /api/v2/communication-adapters?mode=OUTBOUND
```

### 2. Create Adapter

**v1 Request (Deprecated)**
```json
POST /api/v1/communication-adapters
{
  "name": "FTP Inbound",
  "type": "FTP",
  "mode": "SENDER",
  "configuration": "{...}",
  "businessComponentId": "123"
}
```

**v2 Request (Recommended)**
```json
POST /api/v2/communication-adapters
{
  "name": "FTP Inbound",
  "type": "FTP",
  "mode": "INBOUND",
  "configuration": "{...}",
  "businessComponentId": "123"
}
```

### 3. Response Format Changes

**v1 Response (Deprecated)**
```json
{
  "id": "456",
  "name": "FTP Inbound",
  "type": "FTP",
  "mode": "SENDER",
  "status": "ACTIVE"
}
```

**v2 Response (Recommended)**
```json
{
  "id": "456",
  "name": "FTP Inbound",
  "type": "FTP",
  "mode": "INBOUND",
  "status": "ACTIVE"
}
```

## Integration Flow Changes

### Database Column Names
- `source_adapter_id` → `inbound_adapter_id`
- `target_adapter_id` → `outbound_adapter_id`

### API Response Example

**Old Response Structure**
```json
{
  "id": "flow-123",
  "name": "FTP to Database Flow",
  "sourceAdapterId": "adapter-456",
  "targetAdapterId": "adapter-789"
}
```

**New Response Structure**
```json
{
  "id": "flow-123",
  "name": "FTP to Database Flow",
  "inboundAdapterId": "adapter-456",
  "outboundAdapterId": "adapter-789"
}
```

## Migration Steps for API Consumers

### Step 1: Update Base URLs
```javascript
// Old
const API_BASE = '/api/communication-adapters';
const API_V1 = '/api/v1/communication-adapters';

// New
const API_V2 = '/api/v2/communication-adapters';
```

### Step 2: Update Request Payloads
```javascript
// Old
const createAdapter = {
  mode: 'SENDER',
  // ... other fields
};

// New
const createAdapter = {
  mode: 'INBOUND',
  // ... other fields
};
```

### Step 3: Update Response Handling
```javascript
// Old
if (adapter.mode === 'SENDER') {
  // handle inbound adapter
}

// New
if (adapter.mode === 'INBOUND') {
  // handle inbound adapter
}
```

### Step 4: Update Flow References
```javascript
// Old
const flow = {
  sourceAdapterId: 'adapter-123',
  targetAdapterId: 'adapter-456'
};

// New
const flow = {
  inboundAdapterId: 'adapter-123',
  outboundAdapterId: 'adapter-456'
};
```

## Backward Compatibility

### API v1 Features
- Automatically converts SENDER → INBOUND in requests
- Automatically converts INBOUND → SENDER in responses
- Logs deprecation warnings
- Will be maintained for 12 months

### Headers
v1 API responses include deprecation headers:
```
Deprecation: true
Sunset: Sat, 1 Sep 2026 00:00:00 GMT
Link: </api/v2/communication-adapters>; rel="successor-version"
```

## Error Handling

### v2 API Validation
If you use old terminology with v2 API:
```json
{
  "error": "Invalid adapter mode for API v2. Use 'INBOUND' or 'OUTBOUND'. For legacy support with 'SENDER'/'RECEIVER', use API v1.",
  "status": 400
}
```

## SDK Updates

### Java Client
```java
// Old
AdapterMode.SENDER
AdapterMode.RECEIVER

// New
AdapterMode.INBOUND
AdapterMode.OUTBOUND
```

### TypeScript Client
```typescript
// Old
enum AdapterMode {
  SENDER = "SENDER",
  RECEIVER = "RECEIVER"
}

// New
enum AdapterMode {
  INBOUND = "INBOUND",
  OUTBOUND = "OUTBOUND"
}
```

## Testing Your Migration

### 1. Test v2 Endpoints
```bash
# Test creating an adapter with new terminology
curl -X POST http://localhost:8080/api/v2/communication-adapters \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Adapter",
    "type": "FTP",
    "mode": "INBOUND",
    "configuration": "{}",
    "businessComponentId": "123"
  }'
```

### 2. Verify Backward Compatibility
```bash
# Test v1 endpoint still works with old terminology
curl -X GET http://localhost:8080/api/v1/communication-adapters?mode=SENDER
```

### 3. Check Deprecation Headers
```bash
# Look for deprecation headers in v1 responses
curl -I http://localhost:8080/api/v1/communication-adapters
```

## Support

For questions or issues during migration:
- Documentation: [Link to docs]
- Support Email: [support@example.com]
- Migration Hotline: [Phone number]

## Frequently Asked Questions

**Q: Will my existing integrations break?**
A: No, v1 API will continue to work for 12 months with automatic translation.

**Q: Do I need to update my database?**
A: The database migration is handled server-side. Your API calls will work with both old and new column names during the transition.

**Q: Can I use both APIs simultaneously?**
A: Yes, you can gradually migrate endpoints from v1 to v2.

**Q: What happens after v1 is removed?**
A: Only v2 endpoints will be available. Any calls to v1 will return 404 Not Found.