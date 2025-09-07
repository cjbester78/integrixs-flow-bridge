# Adapter Configuration Integration with Native XML Transformation

## Overview

This document explains how the new extensible adapter architecture integrates with the existing Native XML mapping and transformation system in Integrix Flow Bridge.

## Current Architecture

### Message Structures (for non-SOAP adapters)
- Store XSD schemas that define data structures
- Used by File, REST, Database adapters
- Converted to internal XML format for transformation

### Flow Structures (for SOAP adapters)  
- Store WSDL definitions
- Define operations, input/output messages
- Native XML processing for SOAP services

## Integration Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Adapter Instance                          │
├─────────────────────────────────────────────────────────────────┤
│  Configuration     →  Payload Structure  →  Transformation      │
│  (Connection)         (XSD/WSDL/JSON)       (XML Mapping)       │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                   Transformation Engine                          │
├─────────────────────────────────────────────────────────────────┤
│  Source Format  →  Native XML  →  Target Format                 │
│  (Any)             (Internal)     (Any)                          │
└─────────────────────────────────────────────────────────────────┘
```

## Enhanced Adapter Configuration Schema

```json
{
  "code": "salesforce",
  "name": "Salesforce",
  "configuration_schema": {
    "sections": [
      {
        "id": "connection",
        "title": "Connection Settings",
        "fields": [...]
      },
      {
        "id": "payload_structure",
        "title": "Data Structure Definition",
        "fields": [
          {
            "name": "structureType",
            "type": "select",
            "label": "Structure Type",
            "required": true,
            "options": [
              {"value": "message_structure", "label": "Message Structure (XSD)"},
              {"value": "flow_structure", "label": "Flow Structure (WSDL)"},
              {"value": "json_schema", "label": "JSON Schema"},
              {"value": "auto_discover", "label": "Auto-discover from endpoint"},
              {"value": "custom", "label": "Define custom structure"}
            ]
          },
          {
            "name": "messageStructureId",
            "type": "dynamic-select",
            "label": "Select Message Structure",
            "required": true,
            "condition": {
              "field": "structureType",
              "value": "message_structure"
            },
            "dynamicOptions": {
              "endpoint": "/api/message-structures",
              "params": {
                "businessComponentId": "${businessComponentId}"
              }
            }
          },
          {
            "name": "flowStructureId",
            "type": "dynamic-select",
            "label": "Select Flow Structure",
            "required": true,
            "condition": {
              "field": "structureType",
              "value": "flow_structure"
            },
            "dynamicOptions": {
              "endpoint": "/api/flow-structures",
              "params": {
                "businessComponentId": "${businessComponentId}",
                "direction": "${direction}"
              }
            }
          },
          {
            "name": "autoDiscoverySettings",
            "type": "group",
            "label": "Auto-Discovery Settings",
            "condition": {
              "field": "structureType",
              "value": "auto_discover"
            },
            "fields": [
              {
                "name": "discoveryMethod",
                "type": "select",
                "label": "Discovery Method",
                "options": [
                  {"value": "metadata_api", "label": "Metadata API"},
                  {"value": "sample_data", "label": "From Sample Data"},
                  {"value": "openapi", "label": "OpenAPI/Swagger"}
                ]
              },
              {
                "name": "sampleEndpoint",
                "type": "text",
                "label": "Sample Endpoint",
                "placeholder": "/api/v2/Account"
              }
            ]
          },
          {
            "name": "schemaUpload",
            "type": "file-upload",
            "label": "Upload Schema",
            "condition": {
              "field": "structureType",
              "value": "custom"
            },
            "accept": ".xsd,.wsdl,.json",
            "maxSize": "10MB"
          }
        ]
      },
      {
        "id": "transformation_settings",
        "title": "Transformation Settings",
        "fields": [
          {
            "name": "enableTransformation",
            "type": "boolean",
            "label": "Enable Transformation",
            "default": true
          },
          {
            "name": "transformationMode",
            "type": "select",
            "label": "Transformation Mode",
            "condition": {
              "field": "enableTransformation",
              "value": true
            },
            "options": [
              {"value": "native_xml", "label": "Native XML (Default)"},
              {"value": "direct", "label": "Direct Format"},
              {"value": "custom", "label": "Custom Processor"}
            ]
          },
          {
            "name": "skipXmlConversion",
            "type": "boolean",
            "label": "Skip XML Conversion",
            "default": false,
            "help": "For adapters that work directly with XML"
          }
        ]
      }
    ]
  }
}
```

## Database Schema Extensions

```sql
-- Extend adapter_types table
ALTER TABLE adapter_types ADD COLUMN structure_handling JSONB;

-- Example structure_handling configuration
{
  "supported_structures": ["message_structure", "flow_structure", "json_schema"],
  "auto_discovery": {
    "supported": true,
    "methods": ["metadata_api", "sample_data", "openapi"]
  },
  "native_formats": ["xml", "json", "csv", "edifact", "x12"],
  "transformation_required": true,
  "direct_xml_support": false
}

-- Extend adapter_instances table  
ALTER TABLE adapter_instances ADD COLUMN structure_configuration JSONB;

-- Example structure_configuration
{
  "inbound": {
    "structureType": "message_structure",
    "messageStructureId": "uuid-123",
    "transformationEnabled": true,
    "transformationMode": "native_xml"
  },
  "outbound": {
    "structureType": "auto_discover",
    "discoverySettings": {
      "method": "metadata_api",
      "cachedSchema": {...},
      "lastDiscovered": "2024-01-15T10:00:00Z"
    },
    "transformationEnabled": true
  }
}
```

## Transformation Flow

### 1. Inbound Flow (External → Internal)

```
External System → Adapter → Format Detection → Structure Validation → XML Conversion → Transformation → Target System

Example: Salesforce → REST Adapter → JSON → Validate against schema → Convert to XML → Apply mappings → SAP
```

### 2. Outbound Flow (Internal → External)

```
Source System → XML Format → Transformation → Target Structure → Format Conversion → Adapter → External System

Example: SAP → XML → Apply mappings → Target schema → Convert to JSON → REST Adapter → Salesforce
```

## Implementation Details

### 1. Structure Resolution Service

```java
@Service
public class AdapterStructureService {
    
    public StructureDefinition resolveStructure(AdapterInstance adapter, Direction direction) {
        StructureConfiguration config = adapter.getStructureConfiguration(direction);
        
        switch (config.getStructureType()) {
            case MESSAGE_STRUCTURE:
                return messageStructureService.getById(config.getMessageStructureId());
                
            case FLOW_STRUCTURE:
                return flowStructureService.getById(config.getFlowStructureId());
                
            case AUTO_DISCOVER:
                return autoDiscoveryService.discoverStructure(
                    adapter,
                    config.getDiscoverySettings()
                );
                
            case JSON_SCHEMA:
                return jsonSchemaService.convertToStructure(config.getJsonSchema());
                
            case CUSTOM:
                return customStructureService.parse(config.getCustomSchema());
        }
    }
}
```

### 2. Auto-Discovery Implementation

```java
@Service
public class StructureAutoDiscoveryService {
    
    public StructureDefinition discoverStructure(AdapterInstance adapter, DiscoverySettings settings) {
        AdapterPlugin plugin = pluginRegistry.getPlugin(adapter.getTypeCode());
        
        switch (settings.getMethod()) {
            case METADATA_API:
                // Use adapter-specific metadata API
                return plugin.discoverStructureFromMetadata(adapter.getConfiguration());
                
            case SAMPLE_DATA:
                // Analyze sample response
                SampleData sample = plugin.fetchSampleData(settings.getSampleEndpoint());
                return structureInferenceService.inferFromSample(sample);
                
            case OPENAPI:
                // Parse OpenAPI/Swagger definition
                OpenAPISpec spec = plugin.getOpenAPISpecification();
                return openApiParser.parseToStructure(spec, settings.getOperationId());
        }
    }
}
```

### 3. Transformation Integration

```java
@Component
public class AdapterTransformationHandler {
    
    public Message transform(Message input, AdapterInstance adapter, Direction direction) {
        // Get structure definitions
        StructureDefinition sourceStructure = structureService.getSourceStructure(adapter, direction);
        StructureDefinition targetStructure = structureService.getTargetStructure(adapter, direction);
        
        // Check if transformation is needed
        if (!adapter.isTransformationEnabled(direction)) {
            return input;
        }
        
        // Convert to Native XML if needed
        Message xmlMessage = input;
        if (!input.isXmlFormat() && !adapter.skipXmlConversion()) {
            xmlMessage = xmlConverter.convertToXml(input, sourceStructure);
        }
        
        // Apply transformation mappings
        TransformationDefinition transformation = transformationService.getTransformation(
            sourceStructure.getId(),
            targetStructure.getId()
        );
        
        Message transformed = transformationEngine.transform(xmlMessage, transformation);
        
        // Convert from XML to target format if needed
        if (!targetStructure.isXmlFormat()) {
            transformed = formatConverter.convertFromXml(transformed, targetStructure);
        }
        
        return transformed;
    }
}
```

## UI Integration

### 1. Adapter Configuration Page

```typescript
// Enhanced adapter configuration component
export const AdapterConfiguration = () => {
  const [structureType, setStructureType] = useState('message_structure');
  const [selectedStructure, setSelectedStructure] = useState(null);
  
  return (
    <Tabs>
      <TabsList>
        <TabsTrigger value="connection">Connection</TabsTrigger>
        <TabsTrigger value="structure">Data Structure</TabsTrigger>
        <TabsTrigger value="transformation">Transformation</TabsTrigger>
        <TabsTrigger value="advanced">Advanced</TabsTrigger>
      </TabsList>
      
      <TabsContent value="structure">
        <StructureConfiguration
          adapterType={adapterType}
          onStructureSelect={setSelectedStructure}
        />
        {selectedStructure && (
          <StructurePreview structure={selectedStructure} />
        )}
      </TabsContent>
      
      <TabsContent value="transformation">
        <TransformationSettings
          sourceStructure={selectedStructure}
          enableMapping={true}
        />
      </TabsContent>
    </Tabs>
  );
};
```

### 2. Structure Selection Component

```typescript
export const StructureSelection = ({ adapterType, direction, onChange }) => {
  const { structureHandling } = adapterType;
  const [structureType, setStructureType] = useState('message_structure');
  
  const handleAutoDiscover = async () => {
    const discovered = await adapterApi.discoverStructure({
      adapterTypeId: adapterType.id,
      method: 'metadata_api',
      connectionConfig: currentConfig
    });
    
    onChange({
      type: 'auto_discover',
      schema: discovered.schema,
      fields: discovered.fields
    });
  };
  
  return (
    <div>
      <Select value={structureType} onChange={setStructureType}>
        {structureHandling.supported_structures.map(type => (
          <SelectItem key={type} value={type}>
            {getStructureTypeLabel(type)}
          </SelectItem>
        ))}
      </Select>
      
      {structureType === 'message_structure' && (
        <MessageStructureSelector
          businessComponentId={businessComponentId}
          onChange={(structure) => onChange({ type: 'message_structure', id: structure.id })}
        />
      )}
      
      {structureType === 'auto_discover' && (
        <Button onClick={handleAutoDiscover}>
          Discover Structure from Endpoint
        </Button>
      )}
    </div>
  );
};
```

## Benefits of Integration

1. **Consistency**: All adapters use the same structure definition mechanism
2. **Flexibility**: Support for multiple structure formats (XSD, WSDL, JSON Schema)
3. **Auto-Discovery**: Reduces manual configuration for modern APIs
4. **Reusability**: Share structures across adapters
5. **Native XML Core**: Maintains the powerful XML transformation engine
6. **Format Agnostic**: Automatic conversion to/from XML as needed

## Migration Path

1. Existing adapters continue to work with current message/flow structures
2. New adapters can use enhanced structure configuration
3. Gradual migration of existing adapters to new schema
4. Backward compatibility maintained throughout

## Example: Salesforce to SAP Integration

```yaml
Flow: Salesforce Account → SAP Customer
  
1. Salesforce Adapter (Inbound):
   - Structure: Auto-discovered from Salesforce API
   - Format: JSON
   - Sample: {"Id": "001xxx", "Name": "Acme Corp", "Type": "Customer"}

2. XML Conversion:
   - JSON → Native XML
   - Applied namespaces from discovered schema
   
3. Transformation:
   - Source: Salesforce Account XML
   - Target: SAP Customer XML (from Message Structure)
   - Mappings: Visual field mapping
   
4. SAP Adapter (Outbound):
   - Structure: IDoc XSD (Message Structure)
   - Format: IDoc XML
   - Direct send (no conversion needed)
```

This integration ensures the new adapter architecture seamlessly works with the existing XML transformation system while adding modern capabilities like auto-discovery and format flexibility.