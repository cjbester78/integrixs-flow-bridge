-- Test database initialization script

-- Create test schema
CREATE SCHEMA IF NOT EXISTS integrix;

-- Create test tables (minimal subset for integration tests)
CREATE TABLE IF NOT EXISTS integration_flows (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    source_adapter_id VARCHAR(36),
    target_adapter_id VARCHAR(36),
    retry_attempts INT DEFAULT 3,
    retry_delay BIGINT DEFAULT 1000,
    timeout_ms BIGINT DEFAULT 30000,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    CONSTRAINT uk_flow_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS communication_adapters (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    adapter_type VARCHAR(50) NOT NULL,
    direction VARCHAR(50) NOT NULL,
    configuration JSON,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_adapter_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS field_mappings (
    id VARCHAR(36) PRIMARY KEY,
    flow_id VARCHAR(36) NOT NULL,
    source_field VARCHAR(255) NOT NULL,
    target_field VARCHAR(255) NOT NULL,
    transformation_type VARCHAR(50),
    transformation_expression TEXT,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_flow_id FOREIGN KEY (flow_id) REFERENCES integration_flows(id)
);

CREATE TABLE IF NOT EXISTS flow_executions (
    id VARCHAR(36) PRIMARY KEY,
    flow_id VARCHAR(36) NOT NULL,
    status VARCHAR(50) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    error_message TEXT,
    error_code VARCHAR(50),
    input_data JSON,
    output_data JSON,
    CONSTRAINT fk_execution_flow_id FOREIGN KEY (flow_id) REFERENCES integration_flows(id)
);

CREATE TABLE IF NOT EXISTS system_logs (
    id BIGSERIAL PRIMARY KEY,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    module VARCHAR(100) NOT NULL,
    operation VARCHAR(255) NOT NULL,
    context JSON,
    level VARCHAR(20) NOT NULL,
    message TEXT,
    error_details TEXT
);

CREATE TABLE IF NOT EXISTS messages (
    id VARCHAR(36) PRIMARY KEY,
    flow_id VARCHAR(36),
    content TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    processed_at TIMESTAMP,
    error_message TEXT
);

CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS system_configuration (
    key VARCHAR(255) PRIMARY KEY,
    value TEXT NOT NULL,
    description TEXT,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_flow_status ON integration_flows(status);
CREATE INDEX idx_adapter_type ON communication_adapters(adapter_type);
CREATE INDEX idx_adapter_direction ON communication_adapters(direction);
CREATE INDEX idx_field_mapping_flow ON field_mappings(flow_id);
CREATE INDEX idx_flow_execution_flow ON flow_executions(flow_id);
CREATE INDEX idx_flow_execution_status ON flow_executions(status);
CREATE INDEX idx_system_logs_module ON system_logs(module);
CREATE INDEX idx_system_logs_timestamp ON system_logs(timestamp);
CREATE INDEX idx_messages_flow ON messages(flow_id);
CREATE INDEX idx_messages_status ON messages(status);

-- Insert test data
INSERT INTO system_configuration (key, value, description) VALUES
    ('environment.type', 'DEVELOPMENT', 'Environment type'),
    ('jwt.secret', 'test-secret-key-for-integration-tests', 'JWT secret key'),
    ('jwt.expiration', '3600000', 'JWT token expiration in milliseconds');

-- Insert test user
INSERT INTO users (id, username, email, password, role, active, created_at, updated_at) VALUES
    ('test-admin-id', 'admin', 'admin@test.com', '$2a$10$test-password-hash', 'ADMINISTRATOR', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);