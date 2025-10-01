-- ========================================
-- Integrix Flow Bridge Database Schema
-- ========================================
-- This script creates all tables for the application
-- Version: 2.0.0
-- ========================================

-- Enable UUID extension if not already enabled
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ========================================
-- Core Tables
-- ========================================

-- Roles table
CREATE TABLE IF NOT EXISTS roles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    permissions JSONB DEFAULT '[]'::jsonb,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role_id UUID REFERENCES roles(id),
    is_active BOOLEAN DEFAULT true,
    is_locked BOOLEAN DEFAULT false,
    failed_login_attempts INTEGER DEFAULT 0,
    last_login_at TIMESTAMP,
    last_login_ip VARCHAR(45),
    password_changed_at TIMESTAMP,
    activation_token VARCHAR(255),
    activation_token_expires_at TIMESTAMP,
    reset_token VARCHAR(255),
    reset_token_expires_at TIMESTAMP,
    preferences JSONB DEFAULT '{}'::jsonb,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User sessions table
CREATE TABLE IF NOT EXISTS user_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    session_token VARCHAR(255) NOT NULL UNIQUE,
    ip_address VARCHAR(45),
    user_agent TEXT,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Business components table
CREATE TABLE IF NOT EXISTS business_components (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    contact_email VARCHAR(255) NOT NULL,
    contact_phone VARCHAR(50),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    department VARCHAR(100),
    metadata JSONB DEFAULT '{}'::jsonb,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- ========================================
-- Integration Flow Tables
-- ========================================

-- Integration flows table
CREATE TABLE IF NOT EXISTS integration_flows (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    business_component_id UUID NOT NULL REFERENCES business_components(id),
    flow_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'DRAFT',
    version INTEGER DEFAULT 1,
    source_adapter_id UUID,
    target_adapter_id UUID,
    transformation_id UUID,
    flow_configuration JSONB DEFAULT '{}'::jsonb,
    is_active BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_flow_name_component UNIQUE (name, business_component_id)
);

-- Communication adapters table
CREATE TABLE IF NOT EXISTS communication_adapters (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    adapter_type VARCHAR(50) NOT NULL,
    business_component_id UUID NOT NULL REFERENCES business_components(id),
    configuration JSONB DEFAULT '{}'::jsonb,
    status VARCHAR(50) DEFAULT 'INACTIVE',
    version VARCHAR(20),
    health_status VARCHAR(50) DEFAULT 'UNKNOWN',
    last_health_check TIMESTAMP,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_adapter_name_component UNIQUE (name, business_component_id)
);

-- ========================================
-- Message and Structure Tables
-- ========================================

-- Messages table
CREATE TABLE IF NOT EXISTS messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    flow_id UUID NOT NULL REFERENCES integration_flows(id),
    correlation_id VARCHAR(255),
    message_type VARCHAR(50),
    status VARCHAR(50) DEFAULT 'RECEIVED',
    source_system VARCHAR(100),
    target_system VARCHAR(100),
    payload TEXT,
    payload_size BIGINT,
    payload_format VARCHAR(50),
    headers JSONB DEFAULT '{}'::jsonb,
    error_message TEXT,
    error_stacktrace TEXT,
    retry_count INTEGER DEFAULT 0,
    processed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Message structures table
CREATE TABLE IF NOT EXISTS message_structures (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    structure_type VARCHAR(50) NOT NULL,
    xsd_content TEXT,
    sample_xml TEXT,
    validation_enabled BOOLEAN DEFAULT true,
    version VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- Flow structures table (for WSDL-based flows)
CREATE TABLE IF NOT EXISTS flow_structures (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    wsdl_content TEXT,
    service_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- ========================================
-- Transformation and Mapping Tables
-- ========================================

-- Flow transformations table
CREATE TABLE IF NOT EXISTS flow_transformations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    transformation_type VARCHAR(50) NOT NULL,
    source_structure_id UUID,
    target_structure_id UUID,
    xslt_content TEXT,
    custom_code TEXT,
    configuration JSONB DEFAULT '{}'::jsonb,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- Field mappings table
CREATE TABLE IF NOT EXISTS field_mappings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    transformation_id UUID NOT NULL REFERENCES flow_transformations(id) ON DELETE CASCADE,
    source_field VARCHAR(500),
    target_field VARCHAR(500),
    mapping_type VARCHAR(50),
    transformation_function VARCHAR(255),
    function_parameters JSONB DEFAULT '{}'::jsonb,
    is_mandatory BOOLEAN DEFAULT false,
    default_value VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- Monitoring and Health Tables
-- ========================================

-- Adapter health records table
CREATE TABLE IF NOT EXISTS adapter_health_records (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    adapter_id UUID NOT NULL REFERENCES communication_adapters(id),
    health_status VARCHAR(50) NOT NULL,
    response_time_ms INTEGER,
    memory_usage_mb INTEGER,
    cpu_usage_percent NUMERIC(5,2),
    active_connections INTEGER,
    error_count INTEGER DEFAULT 0,
    warning_count INTEGER DEFAULT 0,
    details JSONB DEFAULT '{}'::jsonb,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Flow execution records table
CREATE TABLE IF NOT EXISTS flow_executions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    flow_id UUID NOT NULL REFERENCES integration_flows(id),
    execution_id VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    duration_ms BIGINT,
    records_processed INTEGER DEFAULT 0,
    records_failed INTEGER DEFAULT 0,
    error_message TEXT,
    execution_metadata JSONB DEFAULT '{}'::jsonb,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- System logs table
CREATE TABLE IF NOT EXISTS system_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    log_level VARCHAR(20) NOT NULL,
    logger_name VARCHAR(255),
    message TEXT NOT NULL,
    exception_message TEXT,
    stack_trace TEXT,
    context_data JSONB DEFAULT '{}'::jsonb,
    thread_name VARCHAR(255),
    user_id UUID,
    session_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- Configuration Tables
-- ========================================

-- System configuration table
CREATE TABLE IF NOT EXISTS system_configurations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    config_key VARCHAR(255) NOT NULL UNIQUE,
    config_value TEXT,
    config_type VARCHAR(50) NOT NULL,
    category VARCHAR(100),
    description TEXT,
    is_encrypted BOOLEAN DEFAULT false,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- System settings table (for UI-editable settings)
CREATE TABLE IF NOT EXISTS system_settings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    setting_key VARCHAR(255) NOT NULL UNIQUE,
    setting_value TEXT,
    setting_type VARCHAR(50) NOT NULL,
    category VARCHAR(100),
    description TEXT,
    default_value TEXT,
    validation_regex VARCHAR(500),
    is_required BOOLEAN DEFAULT false,
    is_visible BOOLEAN DEFAULT true,
    display_order INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- ========================================
-- Security and Audit Tables
-- ========================================

-- Audit trail table
CREATE TABLE IF NOT EXISTS audit_trails (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    user_id UUID,
    username VARCHAR(255),
    old_values JSONB,
    new_values JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Certificates table
CREATE TABLE IF NOT EXISTS certificates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    certificate_type VARCHAR(50) NOT NULL,
    certificate_data TEXT NOT NULL,
    private_key TEXT,
    certificate_chain TEXT,
    issuer VARCHAR(255),
    subject VARCHAR(255),
    serial_number VARCHAR(100),
    valid_from TIMESTAMP,
    valid_to TIMESTAMP,
    fingerprint VARCHAR(255),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- ========================================
-- Marketplace Tables
-- ========================================

-- Organizations table
CREATE TABLE IF NOT EXISTS organizations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    website VARCHAR(500),
    email VARCHAR(255) NOT NULL,
    contact_person VARCHAR(255),
    logo_url VARCHAR(500),
    verified BOOLEAN DEFAULT false,
    verified_at TIMESTAMP,
    verification_details TEXT,
    template_count INTEGER DEFAULT 0,
    download_count INTEGER DEFAULT 0,
    average_rating DECIMAL(3,2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_organization_name UNIQUE (name),
    CONSTRAINT uk_organization_email UNIQUE (email)
);

-- Flow templates table
CREATE TABLE IF NOT EXISTS flow_templates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    short_description VARCHAR(500),
    organization_id UUID REFERENCES organizations(id),
    author_id UUID REFERENCES users(id),
    category VARCHAR(50) NOT NULL,
    type VARCHAR(50) NOT NULL,
    visibility VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'DRAFT',
    icon_url VARCHAR(500),
    version VARCHAR(50),
    compatible_versions VARCHAR(500),
    tags TEXT,
    screenshots TEXT,
    documentation TEXT,
    flow_definition TEXT,
    configuration TEXT,
    requirements TEXT,
    dependencies TEXT,
    is_certified BOOLEAN DEFAULT false,
    certified_at TIMESTAMP,
    is_featured BOOLEAN DEFAULT false,
    featured_until TIMESTAMP,
    download_count INTEGER DEFAULT 0,
    install_count INTEGER DEFAULT 0,
    average_rating DECIMAL(3,2) DEFAULT 0.00,
    rating_count INTEGER DEFAULT 0,
    published_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- ========================================
-- Advanced Features Tables
-- ========================================

-- Retry policies table
CREATE TABLE IF NOT EXISTS retry_policies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    max_attempts INTEGER DEFAULT 3,
    initial_delay_ms INTEGER DEFAULT 1000,
    max_delay_ms INTEGER DEFAULT 60000,
    multiplier NUMERIC(3,2) DEFAULT 2.0,
    retry_on_errors TEXT[],
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- Alert rules table
CREATE TABLE IF NOT EXISTS alert_rules (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    rule_type VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50),
    condition_expression TEXT NOT NULL,
    threshold_value VARCHAR(100),
    severity VARCHAR(20) NOT NULL,
    notification_channels TEXT[],
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- Alerts table
CREATE TABLE IF NOT EXISTS alerts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    alert_rule_id UUID REFERENCES alert_rules(id),
    entity_type VARCHAR(50),
    entity_id UUID,
    severity VARCHAR(20) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT,
    details JSONB DEFAULT '{}'::jsonb,
    status VARCHAR(50) DEFAULT 'OPEN',
    acknowledged_by UUID,
    acknowledged_at TIMESTAMP,
    resolved_by UUID,
    resolved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Notification channels table
CREATE TABLE IF NOT EXISTS notification_channels (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL UNIQUE,
    channel_type VARCHAR(50) NOT NULL,
    configuration JSONB NOT NULL,
    is_active BOOLEAN DEFAULT true,
    test_mode BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- Dead letter queue table
CREATE TABLE IF NOT EXISTS dead_letter_messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    original_message_id UUID,
    flow_id UUID REFERENCES integration_flows(id),
    correlation_id VARCHAR(255),
    payload TEXT,
    error_message TEXT,
    error_count INTEGER DEFAULT 1,
    last_error_at TIMESTAMP,
    reprocess_after TIMESTAMP,
    status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- Adapter-specific Tables
-- ========================================

-- Adapter types table
CREATE TABLE IF NOT EXISTS adapter_types (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50),
    icon VARCHAR(255),
    configuration_schema JSONB,
    supported_operations TEXT[],
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Adapter payloads table (for large payloads)
CREATE TABLE IF NOT EXISTS adapter_payloads (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    message_id UUID REFERENCES messages(id) ON DELETE CASCADE,
    payload_type VARCHAR(50),
    payload_data TEXT,
    compressed BOOLEAN DEFAULT false,
    encryption_type VARCHAR(50),
    checksum VARCHAR(255),
    size_bytes BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- JAR files table (for custom adapters/transformations)
CREATE TABLE IF NOT EXISTS jar_files (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT,
    file_hash VARCHAR(255),
    version VARCHAR(50),
    main_class VARCHAR(500),
    dependencies TEXT[],
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- ========================================
-- Create Indexes
-- ========================================

-- User indexes
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role_id ON users(role_id);
CREATE INDEX idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX idx_user_sessions_token ON user_sessions(session_token);

-- Business component indexes
CREATE INDEX idx_business_components_name ON business_components(name);
CREATE INDEX idx_business_components_status ON business_components(status);

-- Integration flow indexes
CREATE INDEX idx_integration_flows_business_component ON integration_flows(business_component_id);
CREATE INDEX idx_integration_flows_status ON integration_flows(status);
CREATE INDEX idx_integration_flows_type ON integration_flows(flow_type);

-- Communication adapter indexes
CREATE INDEX idx_communication_adapters_business_component ON communication_adapters(business_component_id);
CREATE INDEX idx_communication_adapters_type ON communication_adapters(adapter_type);
CREATE INDEX idx_communication_adapters_status ON communication_adapters(status);

-- Message indexes
CREATE INDEX idx_messages_flow_id ON messages(flow_id);
CREATE INDEX idx_messages_correlation_id ON messages(correlation_id);
CREATE INDEX idx_messages_status ON messages(status);
CREATE INDEX idx_messages_created_at ON messages(created_at);

-- Health and monitoring indexes
CREATE INDEX idx_adapter_health_adapter_id ON adapter_health_records(adapter_id);
CREATE INDEX idx_adapter_health_created_at ON adapter_health_records(created_at);
CREATE INDEX idx_flow_executions_flow_id ON flow_executions(flow_id);
CREATE INDEX idx_flow_executions_status ON flow_executions(status);
CREATE INDEX idx_flow_executions_start_time ON flow_executions(start_time);

-- System log indexes
CREATE INDEX idx_system_logs_level ON system_logs(log_level);
CREATE INDEX idx_system_logs_created_at ON system_logs(created_at);
CREATE INDEX idx_system_logs_user_id ON system_logs(user_id);

-- Audit trail indexes
CREATE INDEX idx_audit_trails_entity ON audit_trails(entity_type, entity_id);
CREATE INDEX idx_audit_trails_user_id ON audit_trails(user_id);
CREATE INDEX idx_audit_trails_created_at ON audit_trails(created_at);

-- Configuration indexes
CREATE INDEX idx_system_configurations_key ON system_configurations(config_key);
CREATE INDEX idx_system_configurations_category ON system_configurations(category);
CREATE INDEX idx_system_settings_key ON system_settings(setting_key);
CREATE INDEX idx_system_settings_category ON system_settings(category);

-- Alert indexes
CREATE INDEX idx_alerts_rule_id ON alerts(alert_rule_id);
CREATE INDEX idx_alerts_status ON alerts(status);
CREATE INDEX idx_alerts_severity ON alerts(severity);
CREATE INDEX idx_alerts_created_at ON alerts(created_at);

-- Organizations indexes
CREATE INDEX idx_organizations_verified ON organizations(verified);
CREATE INDEX idx_organizations_name ON organizations(name);
CREATE INDEX idx_organizations_email ON organizations(email);

-- Flow templates indexes
CREATE INDEX idx_flow_templates_slug ON flow_templates(slug);
CREATE INDEX idx_flow_templates_organization_id ON flow_templates(organization_id);
CREATE INDEX idx_flow_templates_author_id ON flow_templates(author_id);
CREATE INDEX idx_flow_templates_category ON flow_templates(category);
CREATE INDEX idx_flow_templates_type ON flow_templates(type);
CREATE INDEX idx_flow_templates_visibility ON flow_templates(visibility);
CREATE INDEX idx_flow_templates_status ON flow_templates(status);
CREATE INDEX idx_flow_templates_is_certified ON flow_templates(is_certified);
CREATE INDEX idx_flow_templates_is_featured ON flow_templates(is_featured);
CREATE INDEX idx_flow_templates_published_at ON flow_templates(published_at);

-- ========================================
-- Create Update Triggers
-- ========================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply update triggers to all relevant tables
CREATE TRIGGER update_roles_updated_at BEFORE UPDATE ON roles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_sessions_updated_at BEFORE UPDATE ON user_sessions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_business_components_updated_at BEFORE UPDATE ON business_components
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_integration_flows_updated_at BEFORE UPDATE ON integration_flows
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_communication_adapters_updated_at BEFORE UPDATE ON communication_adapters
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_messages_updated_at BEFORE UPDATE ON messages
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_message_structures_updated_at BEFORE UPDATE ON message_structures
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_flow_structures_updated_at BEFORE UPDATE ON flow_structures
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_flow_transformations_updated_at BEFORE UPDATE ON flow_transformations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_field_mappings_updated_at BEFORE UPDATE ON field_mappings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_system_configurations_updated_at BEFORE UPDATE ON system_configurations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_system_settings_updated_at BEFORE UPDATE ON system_settings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_certificates_updated_at BEFORE UPDATE ON certificates
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_retry_policies_updated_at BEFORE UPDATE ON retry_policies
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_alert_rules_updated_at BEFORE UPDATE ON alert_rules
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_alerts_updated_at BEFORE UPDATE ON alerts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_notification_channels_updated_at BEFORE UPDATE ON notification_channels
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_dead_letter_messages_updated_at BEFORE UPDATE ON dead_letter_messages
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_adapter_types_updated_at BEFORE UPDATE ON adapter_types
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_jar_files_updated_at BEFORE UPDATE ON jar_files
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_organizations_updated_at BEFORE UPDATE ON organizations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_flow_templates_updated_at BEFORE UPDATE ON flow_templates
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ========================================
-- Add Comments
-- ========================================

COMMENT ON TABLE users IS 'System users with authentication and profile information';
COMMENT ON TABLE roles IS 'User roles with associated permissions';
COMMENT ON TABLE business_components IS 'Organizational units that own integration flows';
COMMENT ON TABLE integration_flows IS 'Integration flow definitions';
COMMENT ON TABLE communication_adapters IS 'Communication adapter configurations';
COMMENT ON TABLE messages IS 'Message processing records';
COMMENT ON TABLE system_logs IS 'Application log entries';
COMMENT ON TABLE audit_trails IS 'Audit trail for entity changes';
COMMENT ON TABLE certificates IS 'SSL/TLS certificates and keys';
COMMENT ON TABLE organizations IS 'Marketplace organizations';
COMMENT ON TABLE flow_templates IS 'Flow templates available in the marketplace';