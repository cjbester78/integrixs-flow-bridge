# Adapters Architecture - Integrix Flow Bridge

## Overview

This document outlines the architecture for implementing an extensible adapter system that supports 1000+ pre-built connectors with dynamic inbound/outbound configurations.

## Table of Contents

1. [Current State Analysis](#current-state-analysis)
2. [Target Architecture](#target-architecture)
3. [Database Schema](#database-schema)
4. [Implementation Phases](#implementation-phases)
5. [Adapter Configuration](#adapter-configuration)
6. [Inbound/Outbound Architecture](#inboundoutbound-architecture)
7. [Plugin System](#plugin-system)
8. [Migration Strategy](#migration-strategy)

## Current State Analysis

### Limitations
- **Hard-coded adapter types** in frontend components
- **No database persistence** for adapter configurations
- **Limited to ~30 adapters** (File, FTP, SFTP, HTTP, SOAP, etc.)
- **Static configuration forms** for each adapter type
- **No plugin architecture** for custom adapters

### Strengths
- Clean separation between inbound/outbound configurations
- Existing API structure for adapter management
- Robust backend service architecture
- Visual configuration interface

## Target Architecture

### Goals
1. Support **1000+ pre-built connectors**
2. **Dynamic adapter registration** without code changes
3. **Unified configuration schema** for all adapters
4. **Plugin-based architecture** for custom adapters
5. **Marketplace-style UI** for adapter discovery
6. **Direction-aware configurations** (inbound/outbound/bidirectional)

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Frontend Layer                            │
├─────────────────────────────────────────────────────────────┤
│  Adapter         Dynamic Form      Configuration            │
│  Marketplace  →  Generator      →  Validator                │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                    API Gateway                               │
├─────────────────────────────────────────────────────────────┤
│  Adapter Type    Adapter          Configuration             │
│  Registry API    Instance API     Template API              │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                    Service Layer                             │
├─────────────────────────────────────────────────────────────┤
│  Adapter Type    Plugin           Configuration             │
│  Service         Registry         Service                   │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                    Data Layer                                │
├─────────────────────────────────────────────────────────────┤
│  adapter_types   adapter_configs  adapter_templates         │
│  adapter_categories               adapter_plugins           │
└─────────────────────────────────────────────────────────────┘
```

## Database Schema

### Core Tables

```sql
-- 1. Adapter Categories (for organization)
CREATE TABLE adapter_categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    icon VARCHAR(50),
    parent_category_id UUID REFERENCES adapter_categories(id),
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Adapter Types Registry
CREATE TABLE adapter_types (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(100) UNIQUE NOT NULL, -- e.g., 'salesforce', 'sap-s4hana'
    name VARCHAR(200) NOT NULL,
    category_id UUID REFERENCES adapter_categories(id),
    vendor VARCHAR(100),
    version VARCHAR(20),
    description TEXT,
    icon VARCHAR(100),
    
    -- Direction support
    supports_inbound BOOLEAN DEFAULT false,
    supports_outbound BOOLEAN DEFAULT false,
    supports_bidirectional BOOLEAN DEFAULT false,
    
    -- Configuration schemas
    inbound_config_schema JSONB,
    outbound_config_schema JSONB,
    common_config_schema JSONB,
    
    -- Capabilities and metadata
    capabilities JSONB, -- features, limitations, requirements
    supported_protocols TEXT[],
    supported_formats TEXT[],
    authentication_methods TEXT[],
    
    -- Documentation and support
    documentation_url TEXT,
    support_url TEXT,
    pricing_tier VARCHAR(50), -- 'free', 'standard', 'premium', 'enterprise'
    
    -- Status
    status VARCHAR(50) DEFAULT 'active', -- 'active', 'beta', 'deprecated', 'inactive'
    is_certified BOOLEAN DEFAULT false,
    certification_date TIMESTAMP,
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id)
);

-- 3. Adapter Configuration Templates
CREATE TABLE adapter_config_templates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    adapter_type_id UUID REFERENCES adapter_types(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    direction VARCHAR(20) CHECK (direction IN ('inbound', 'outbound', 'bidirectional')),
    configuration JSONB NOT NULL,
    is_default BOOLEAN DEFAULT false,
    is_public BOOLEAN DEFAULT true,
    tags TEXT[],
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id)
);

-- 4. Adapter Instances (actual configured adapters)
CREATE TABLE adapter_instances (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    adapter_type_id UUID REFERENCES adapter_types(id),
    business_component_id UUID REFERENCES business_components(id),
    name VARCHAR(255) NOT NULL,
    direction VARCHAR(20) CHECK (direction IN ('inbound', 'outbound', 'bidirectional')),
    
    -- Configurations
    inbound_configuration JSONB,
    outbound_configuration JSONB,
    common_configuration JSONB,
    
    -- Runtime settings
    status VARCHAR(50) DEFAULT 'inactive',
    health_status VARCHAR(50),
    last_health_check TIMESTAMP,
    
    -- Metrics
    total_messages_processed BIGINT DEFAULT 0,
    total_errors BIGINT DEFAULT 0,
    last_execution TIMESTAMP,
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id)
);

-- 5. Adapter Plugin Registry
CREATE TABLE adapter_plugins (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    adapter_type_id UUID REFERENCES adapter_types(id) ON DELETE CASCADE,
    plugin_class VARCHAR(500) NOT NULL,
    plugin_version VARCHAR(20) NOT NULL,
    jar_file_id UUID REFERENCES jar_files(id),
    configuration JSONB,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_adapter_types_category ON adapter_types(category_id);
CREATE INDEX idx_adapter_types_status ON adapter_types(status);
CREATE INDEX idx_adapter_instances_type ON adapter_instances(adapter_type_id);
CREATE INDEX idx_adapter_instances_status ON adapter_instances(status);
```

## Implementation Phases

### Phase 1: Foundation (Weeks 1-4)

#### Goals
- Establish database schema
- Create core backend services
- Implement basic API endpoints

#### Tasks
1. **Database Setup**
   ```sql
   -- Migration: V200__create_adapter_architecture.sql
   -- Create all adapter-related tables
   -- Seed initial categories
   ```

2. **Backend Services**
   ```java
   @Service
   public class AdapterTypeService {
       public Page<AdapterType> getAdapterTypes(Pageable pageable, String category, String search);
       public AdapterType registerAdapterType(AdapterTypeDTO dto);
       public void updateAdapterType(UUID id, AdapterTypeDTO dto);
       public ConfigurationSchema getConfigurationSchema(UUID typeId, Direction direction);
   }
   ```

3. **REST Controllers**
   ```java
   @RestController
   @RequestMapping("/api/adapter-types")
   public class AdapterTypeController {
       @GetMapping
       public ResponseEntity<Page<AdapterType>> getAdapterTypes(...);
       
       @GetMapping("/{id}/schema/{direction}")
       public ResponseEntity<ConfigurationSchema> getSchema(...);
   }
   ```

### Phase 2: Dynamic UI (Weeks 5-8)

#### Goals
- Replace hard-coded adapter forms
- Build dynamic form generator
- Create adapter marketplace

#### Components

1. **Dynamic Form Generator**
   ```typescript
   // components/adapter/DynamicAdapterForm.tsx
   interface DynamicFormProps {
     schema: ConfigurationSchema;
     direction: 'inbound' | 'outbound';
     values: Record<string, any>;
     onChange: (values: Record<string, any>) => void;
   }
   
   export const DynamicAdapterForm: React.FC<DynamicFormProps> = ({
     schema,
     direction,
     values,
     onChange
   }) => {
     // Render fields based on schema
     // Handle conditional fields
     // Validate inputs
   };
   ```

2. **Adapter Marketplace**
   ```typescript
   // pages/AdapterMarketplace.tsx
   export const AdapterMarketplace = () => {
     const [category, setCategory] = useState<string>('all');
     const [search, setSearch] = useState('');
     const { data: adapterTypes } = useAdapterTypes({ category, search });
     
     return (
       <div className="adapter-marketplace">
         <CategoryFilter />
         <SearchBar />
         <AdapterGrid adapters={adapterTypes} />
       </div>
     );
   };
   ```

### Phase 3: Adapter Seeding (Weeks 9-10)

#### Goals
- Populate database with 1000+ adapter types
- Create configuration schemas for each
- Establish certification process

#### Adapter Categories to Seed

1. **CRM & Sales (50+ adapters)**
   - Salesforce (all clouds)
   - Microsoft Dynamics 365
   - HubSpot
   - Pipedrive
   - Zoho CRM

2. **ERP & Finance (100+ adapters)**
   - SAP S/4HANA, ECC
   - Oracle Cloud ERP
   - Microsoft Dynamics
   - NetSuite
   - QuickBooks

3. **Communication (50+ adapters)**
   - Email providers
   - SMS gateways
   - Chat platforms
   - Video conferencing

4. **E-Commerce (40+ adapters)**
   - Shopify
   - Magento
   - WooCommerce
   - Amazon
   - eBay

5. **Databases & Storage (60+ adapters)**
   - All major SQL databases
   - NoSQL databases
   - Cloud storage
   - Data warehouses

### Phase 4: Plugin Architecture (Weeks 11-14)

#### Goals
- Implement plugin system
- Create plugin SDK
- Enable custom adapter development

#### Plugin Interface

```java
public interface AdapterPlugin {
    // Metadata
    AdapterMetadata getMetadata();
    
    // Lifecycle
    void initialize(Map<String, Object> configuration);
    void destroy();
    
    // Direction-specific operations
    InboundHandler getInboundHandler();
    OutboundHandler getOutboundHandler();
    
    // Testing
    ConnectionTestResult testConnection(Direction direction);
    
    // Health checks
    HealthStatus checkHealth();
}

public interface InboundHandler {
    void startListening(MessageCallback callback);
    void stopListening();
    PollingResult poll(); // For polling-based adapters
}

public interface OutboundHandler {
    SendResult send(Message message);
    BatchSendResult sendBatch(List<Message> messages);
}
```

### Phase 5: Advanced Features (Weeks 15-18)

#### Goals
- Implement advanced capabilities
- Add monitoring and analytics
- Create adapter development tools

#### Features

1. **Adapter Versioning**
   ```sql
   CREATE TABLE adapter_type_versions (
       id UUID PRIMARY KEY,
       adapter_type_id UUID REFERENCES adapter_types(id),
       version VARCHAR(20) NOT NULL,
       changelog TEXT,
       config_schema_changes JSONB,
       migration_script TEXT,
       released_at TIMESTAMP
   );
   ```

2. **Adapter Analytics**
   ```sql
   CREATE TABLE adapter_usage_metrics (
       adapter_instance_id UUID,
       metric_date DATE,
       messages_processed BIGINT,
       errors_count BIGINT,
       avg_response_time_ms DECIMAL,
       uptime_percentage DECIMAL
   );
   ```

3. **Developer Portal**
   - Adapter SDK documentation
   - Testing tools
   - Certification process
   - Marketplace submission

## Adapter Configuration

### Configuration Schema Structure

```json
{
  "common": {
    "fields": [
      {
        "name": "name",
        "type": "text",
        "label": "Adapter Name",
        "required": true,
        "validation": {
          "pattern": "^[a-zA-Z0-9-_]+$",
          "maxLength": 100
        }
      },
      {
        "name": "description",
        "type": "textarea",
        "label": "Description",
        "required": false
      }
    ]
  },
  "inbound": {
    "fields": [
      {
        "name": "polling",
        "type": "group",
        "label": "Polling Configuration",
        "fields": [
          {
            "name": "enabled",
            "type": "boolean",
            "label": "Enable Polling",
            "default": true
          },
          {
            "name": "interval",
            "type": "number",
            "label": "Polling Interval (seconds)",
            "default": 60,
            "min": 10,
            "condition": {
              "field": "polling.enabled",
              "value": true
            }
          }
        ]
      },
      {
        "name": "authentication",
        "type": "dynamic",
        "label": "Authentication",
        "options": [
          {
            "value": "oauth2",
            "fields": [
              {"name": "clientId", "type": "text", "required": true},
              {"name": "clientSecret", "type": "password", "required": true},
              {"name": "scope", "type": "text", "required": false}
            ]
          },
          {
            "value": "apikey",
            "fields": [
              {"name": "apiKey", "type": "password", "required": true},
              {"name": "apiSecret", "type": "password", "required": false}
            ]
          }
        ]
      }
    ]
  },
  "outbound": {
    "fields": [
      {
        "name": "endpoint",
        "type": "text",
        "label": "Endpoint URL",
        "required": true,
        "placeholder": "https://api.example.com/v1"
      },
      {
        "name": "timeout",
        "type": "number",
        "label": "Timeout (ms)",
        "default": 30000,
        "min": 1000,
        "max": 300000
      },
      {
        "name": "retryPolicy",
        "type": "group",
        "label": "Retry Policy",
        "fields": [
          {
            "name": "maxAttempts",
            "type": "number",
            "label": "Max Retry Attempts",
            "default": 3
          },
          {
            "name": "backoffMultiplier",
            "type": "number",
            "label": "Backoff Multiplier",
            "default": 2
          }
        ]
      }
    ]
  }
}
```

## Inbound/Outbound Architecture

### Direction-Aware Configuration

Each adapter type can support different directions with specific configurations:

#### Inbound Configuration
- **Polling-based**: Periodic checks for new data
- **Webhook-based**: Receive real-time events
- **Stream-based**: Continuous data streams
- **File-watch**: Monitor file systems

#### Outbound Configuration
- **Request-Response**: Synchronous API calls
- **Fire-and-Forget**: Asynchronous messaging
- **Batch Processing**: Bulk operations
- **Streaming**: Continuous data push

### Example: Salesforce Adapter

```json
{
  "code": "salesforce",
  "name": "Salesforce",
  "supports_inbound": true,
  "supports_outbound": true,
  "inbound_config_schema": {
    "fields": [
      {
        "name": "mode",
        "type": "select",
        "label": "Inbound Mode",
        "options": [
          {"value": "polling", "label": "Polling"},
          {"value": "streaming", "label": "Streaming API"},
          {"value": "webhook", "label": "Platform Events"}
        ]
      },
      {
        "name": "objects",
        "type": "multiselect",
        "label": "Objects to Monitor",
        "dynamicOptions": "/api/salesforce/objects"
      },
      {
        "name": "query",
        "type": "soql-editor",
        "label": "SOQL Query",
        "condition": {
          "field": "mode",
          "value": "polling"
        }
      }
    ]
  },
  "outbound_config_schema": {
    "fields": [
      {
        "name": "operation",
        "type": "select",
        "label": "Operation",
        "options": [
          {"value": "create", "label": "Create"},
          {"value": "update", "label": "Update"},
          {"value": "upsert", "label": "Upsert"},
          {"value": "delete", "label": "Delete"},
          {"value": "query", "label": "Query"}
        ]
      },
      {
        "name": "object",
        "type": "select",
        "label": "Salesforce Object",
        "dynamicOptions": "/api/salesforce/objects"
      },
      {
        "name": "mappingMode",
        "type": "select",
        "label": "Field Mapping Mode",
        "options": [
          {"value": "auto", "label": "Automatic"},
          {"value": "manual", "label": "Manual"}
        ]
      }
    ]
  }
}
```

## Plugin System

### Plugin Development Kit (PDK)

```java
// Example custom adapter plugin
@AdapterPlugin(
    code = "custom-erp",
    name = "Custom ERP System",
    version = "1.0.0"
)
public class CustomERPAdapter extends AbstractAdapter {
    
    @Override
    public void configure(AdapterConfiguration config) {
        this.endpoint = config.getString("endpoint");
        this.apiKey = config.getSecureString("apiKey");
    }
    
    @Override
    public InboundHandler createInboundHandler() {
        return new CustomERPInboundHandler(this);
    }
    
    @Override
    public OutboundHandler createOutboundHandler() {
        return new CustomERPOutboundHandler(this);
    }
}
```

### Plugin Deployment

1. **Package plugin as JAR**
2. **Upload through admin UI**
3. **Automatic registration**
4. **Hot reload support**

## Migration Strategy

### From Current to New Architecture

1. **Maintain backward compatibility**
   - Keep existing adapter implementations
   - Map to new adapter types
   - Gradual migration

2. **Migration script example**
   ```sql
   -- Migrate existing adapters to new structure
   INSERT INTO adapter_types (code, name, category_id, supports_inbound, supports_outbound)
   SELECT 
     LOWER(type) as code,
     type as name,
     (SELECT id FROM adapter_categories WHERE code = 'legacy'),
     true,
     true
   FROM communication_adapters
   GROUP BY type;
   ```

3. **Feature flags for rollout**
   ```typescript
   const useNewAdapterSystem = useFeatureFlag('new-adapter-system');
   
   return useNewAdapterSystem ? (
     <DynamicAdapterForm />
   ) : (
     <LegacyAdapterForm />
   );
   ```

## Success Metrics

1. **Adapter Coverage**: 1000+ connectors available
2. **Configuration Time**: 80% reduction in adapter setup time
3. **Developer Velocity**: New adapters added in hours, not days
4. **User Satisfaction**: Self-service adapter configuration
5. **System Performance**: No degradation with dynamic loading

## Conclusion

This architecture provides a scalable, maintainable solution for managing thousands of adapters with full inbound/outbound configuration support. The phased approach ensures smooth migration while maintaining system stability.