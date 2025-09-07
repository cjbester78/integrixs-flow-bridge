-- Enable UUID extension if not already enabled
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

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

-- 4. Add columns to existing communication_adapters table (will be renamed to adapter_instances)
ALTER TABLE communication_adapters ADD COLUMN IF NOT EXISTS adapter_type_id UUID REFERENCES adapter_types(id);
ALTER TABLE communication_adapters ADD COLUMN IF NOT EXISTS inbound_configuration JSONB;
ALTER TABLE communication_adapters ADD COLUMN IF NOT EXISTS outbound_configuration JSONB;
ALTER TABLE communication_adapters ADD COLUMN IF NOT EXISTS common_configuration JSONB;
ALTER TABLE communication_adapters ADD COLUMN IF NOT EXISTS health_status VARCHAR(50);
ALTER TABLE communication_adapters ADD COLUMN IF NOT EXISTS last_health_check TIMESTAMP;
ALTER TABLE communication_adapters ADD COLUMN IF NOT EXISTS total_messages_processed BIGINT DEFAULT 0;
ALTER TABLE communication_adapters ADD COLUMN IF NOT EXISTS total_errors BIGINT DEFAULT 0;
ALTER TABLE communication_adapters ADD COLUMN IF NOT EXISTS last_execution TIMESTAMP;

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
CREATE INDEX idx_adapter_types_code ON adapter_types(code);
CREATE INDEX idx_communication_adapters_type ON communication_adapters(adapter_type_id);
CREATE INDEX idx_adapter_config_templates_type ON adapter_config_templates(adapter_type_id);
CREATE INDEX idx_adapter_plugins_type ON adapter_plugins(adapter_type_id);

-- Add updated_at trigger for adapter_types
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_adapter_types_updated_at BEFORE UPDATE
    ON adapter_types FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();