-- H2 compatible schema for testing
-- Based on PostgreSQL schema but adapted for H2 syntax

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'active',
    permissions TEXT,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

-- Roles table
CREATE TABLE IF NOT EXISTS roles (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    permissions TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User roles mapping
CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- System logs
CREATE TABLE IF NOT EXISTS system_logs (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    timestamp TIMESTAMP NOT NULL,
    level VARCHAR(20) NOT NULL,
    message TEXT,
    details TEXT,
    source VARCHAR(100),
    source_id VARCHAR(255),
    source_name VARCHAR(255),
    component VARCHAR(100),
    component_id VARCHAR(255),
    domain_type VARCHAR(100),
    domain_reference_id VARCHAR(255),
    correlation_id VARCHAR(255),
    session_id VARCHAR(255),
    category VARCHAR(100),
    user_id UUID,
    username VARCHAR(100),
    ip_address VARCHAR(45),
    user_agent TEXT,
    url TEXT,
    http_method VARCHAR(10),
    response_status INT,
    execution_time BIGINT,
    error_code VARCHAR(100),
    stack_trace TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Audit trails
CREATE TABLE IF NOT EXISTS audit_trails (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(255) NOT NULL,
    action VARCHAR(50) NOT NULL,
    changes TEXT,
    user_id UUID,
    user_ip VARCHAR(45),
    user_agent TEXT,
    request_id VARCHAR(255),
    session_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Event store
CREATE TABLE IF NOT EXISTS event_store (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    event_id UUID UNIQUE NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id UUID NOT NULL,
    aggregate_version BIGINT NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    event_data TEXT NOT NULL,
    event_metadata TEXT,
    occurred_at TIMESTAMP NOT NULL,
    triggered_by UUID,
    correlation_id UUID,
    sequence_number BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- External authentications
CREATE TABLE IF NOT EXISTS external_authentications (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    configuration TEXT,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Business components
CREATE TABLE IF NOT EXISTS business_components (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    type VARCHAR(50),
    configuration TEXT,
    active BOOLEAN DEFAULT true,
    parent_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    FOREIGN KEY (parent_id) REFERENCES business_components(id)
);

-- Communication adapters
CREATE TABLE IF NOT EXISTS communication_adapters (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    mode VARCHAR(50) NOT NULL,
    direction VARCHAR(50),
    description TEXT,
    configuration TEXT,
    active BOOLEAN DEFAULT true,
    healthy BOOLEAN DEFAULT true,
    last_health_check TIMESTAMP,
    business_component_id UUID,
    external_authentication_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    FOREIGN KEY (business_component_id) REFERENCES business_components(id),
    FOREIGN KEY (external_authentication_id) REFERENCES external_authentications(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- Message structures
CREATE TABLE IF NOT EXISTS message_structures (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    name VARCHAR(255) NOT NULL,
    version VARCHAR(50),
    type VARCHAR(50) NOT NULL,
    format VARCHAR(50) NOT NULL,
    schema_definition TEXT,
    sample_message TEXT,
    validation_rules TEXT,
    transformation_rules TEXT,
    active BOOLEAN DEFAULT true,
    business_component_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    FOREIGN KEY (business_component_id) REFERENCES business_components(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- Flow structures
CREATE TABLE IF NOT EXISTS flow_structures (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    format VARCHAR(50) NOT NULL,
    definition TEXT,
    processing_mode VARCHAR(50),
    validation_rules TEXT,
    active BOOLEAN DEFAULT true,
    business_component_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    FOREIGN KEY (business_component_id) REFERENCES business_components(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- Integration flows
CREATE TABLE IF NOT EXISTS integration_flows (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    active BOOLEAN DEFAULT false,
    flow_type VARCHAR(50),
    mapping_mode VARCHAR(50),
    inbound_adapter_id UUID,
    outbound_adapter_id UUID,
    source_flow_structure_id UUID,
    target_flow_structure_id UUID,
    configuration TEXT,
    execution_count INT DEFAULT 0,
    success_count INT DEFAULT 0,
    error_count INT DEFAULT 0,
    last_execution_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    FOREIGN KEY (inbound_adapter_id) REFERENCES communication_adapters(id),
    FOREIGN KEY (outbound_adapter_id) REFERENCES communication_adapters(id),
    FOREIGN KEY (source_flow_structure_id) REFERENCES flow_structures(id),
    FOREIGN KEY (target_flow_structure_id) REFERENCES flow_structures(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- Messages
CREATE TABLE IF NOT EXISTS messages (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    correlation_id VARCHAR(255) NOT NULL,
    flow_id UUID,
    adapter_id UUID,
    status VARCHAR(50) NOT NULL,
    direction VARCHAR(50),
    payload TEXT,
    headers TEXT,
    error_message TEXT,
    retry_count INT DEFAULT 0,
    priority INT DEFAULT 5,
    received_at TIMESTAMP,
    processed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (flow_id) REFERENCES integration_flows(id),
    FOREIGN KEY (adapter_id) REFERENCES communication_adapters(id)
);

-- Alert rules
CREATE TABLE IF NOT EXISTS alert_rules (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    rule_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    condition_expression TEXT NOT NULL,
    threshold_value DOUBLE,
    threshold_unit VARCHAR(50),
    time_window_minutes INT,
    notification_channels TEXT,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Alerts
CREATE TABLE IF NOT EXISTS alerts (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    alert_id VARCHAR(255) UNIQUE NOT NULL,
    rule_id UUID,
    severity VARCHAR(20) NOT NULL,
    status VARCHAR(50) NOT NULL,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    details TEXT,
    source_type VARCHAR(50),
    source_id VARCHAR(255),
    triggered_at TIMESTAMP NOT NULL,
    acknowledged_at TIMESTAMP,
    acknowledged_by VARCHAR(255),
    resolved_at TIMESTAMP,
    resolved_by VARCHAR(255),
    resolution_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (rule_id) REFERENCES alert_rules(id)
);

-- Additional tables for completeness
CREATE TABLE IF NOT EXISTS adapter_payloads (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    correlation_id VARCHAR(255),
    adapter_id UUID,
    adapter_name VARCHAR(255),
    adapter_type VARCHAR(50),
    direction VARCHAR(50),
    payload_type VARCHAR(50),
    payload TEXT,
    payload_size INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS system_configurations (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    key VARCHAR(255) UNIQUE NOT NULL,
    value TEXT,
    description TEXT,
    category VARCHAR(100),
    data_type VARCHAR(50),
    is_encrypted BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS jar_files (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    file_name VARCHAR(255) NOT NULL,
    display_name VARCHAR(255),
    file_content BLOB,
    file_size BIGINT,
    checksum VARCHAR(255),
    version VARCHAR(50),
    description TEXT,
    uploaded_by VARCHAR(255),
    active BOOLEAN DEFAULT true,
    metadata TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notification_channels (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    configuration TEXT,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS flow_executions (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    flow_id UUID NOT NULL,
    correlation_id VARCHAR(255),
    status VARCHAR(50),
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (flow_id) REFERENCES integration_flows(id)
);

CREATE TABLE IF NOT EXISTS error_records (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    error_type VARCHAR(100),
    error_code VARCHAR(100),
    message TEXT,
    stack_trace TEXT,
    source VARCHAR(100),
    correlation_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_sessions (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    user_id UUID NOT NULL,
    session_token VARCHAR(500),
    ip_address VARCHAR(45),
    user_agent TEXT,
    started_at TIMESTAMP,
    last_activity_at TIMESTAMP,
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS user_preferences (
    user_id UUID PRIMARY KEY,
    preferences TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Child tables for complex entities
CREATE TABLE IF NOT EXISTS flow_transformations (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    flow_id UUID NOT NULL,
    transformation_type VARCHAR(50) NOT NULL,
    execution_order INT NOT NULL,
    configuration TEXT,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (flow_id) REFERENCES integration_flows(id)
);

CREATE TABLE IF NOT EXISTS orchestration_targets (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    flow_id UUID NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_adapter_id UUID,
    target_flow_id UUID,
    execution_order INT,
    condition_expression TEXT,
    max_attempts INT DEFAULT 3,
    retry_delay_seconds INT DEFAULT 60,
    timeout_seconds INT DEFAULT 300,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (flow_id) REFERENCES integration_flows(id),
    FOREIGN KEY (target_adapter_id) REFERENCES communication_adapters(id),
    FOREIGN KEY (target_flow_id) REFERENCES integration_flows(id)
);

CREATE TABLE IF NOT EXISTS flow_structure_messages (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    flow_structure_id UUID NOT NULL,
    message_name VARCHAR(255) NOT NULL,
    message_type VARCHAR(50) NOT NULL,
    direction VARCHAR(50),
    schema_definition TEXT,
    sample_data TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (flow_structure_id) REFERENCES flow_structures(id)
);

CREATE TABLE IF NOT EXISTS flow_structure_namespaces (
    flow_structure_id UUID NOT NULL,
    prefix VARCHAR(50),
    namespace_uri VARCHAR(500) NOT NULL,
    PRIMARY KEY (flow_structure_id, namespace_uri),
    FOREIGN KEY (flow_structure_id) REFERENCES flow_structures(id)
);

CREATE TABLE IF NOT EXISTS message_structure_namespaces (
    message_structure_id UUID NOT NULL,
    prefix VARCHAR(50),
    namespace_uri VARCHAR(500) NOT NULL,
    PRIMARY KEY (message_structure_id, namespace_uri),
    FOREIGN KEY (message_structure_id) REFERENCES message_structures(id)
);

-- Indexes for performance
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_system_logs_timestamp ON system_logs(timestamp);
CREATE INDEX idx_system_logs_correlation_id ON system_logs(correlation_id);
CREATE INDEX idx_audit_trails_entity ON audit_trails(entity_type, entity_id);
CREATE INDEX idx_event_store_aggregate ON event_store(aggregate_id, aggregate_version);
CREATE INDEX idx_messages_correlation_id ON messages(correlation_id);
CREATE INDEX idx_messages_status ON messages(status);