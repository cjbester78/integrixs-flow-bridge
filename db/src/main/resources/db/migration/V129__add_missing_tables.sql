-- V129: Add missing tables for flow execution engine

-- Flow Routes table
CREATE TABLE IF NOT EXISTS flow_routes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    route_name VARCHAR(255) NOT NULL,
    route_type VARCHAR(50) NOT NULL,
    flow_id UUID NOT NULL,
    source_step VARCHAR(255),
    target_step VARCHAR(255),
    is_active BOOLEAN DEFAULT true,
    priority INTEGER DEFAULT 0,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    FOREIGN KEY (flow_id) REFERENCES integration_flows(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- Route Conditions table
CREATE TABLE IF NOT EXISTS route_conditions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    flow_route_id UUID NOT NULL,
    condition_type VARCHAR(50) NOT NULL,
    field_path VARCHAR(255) NOT NULL,
    operator VARCHAR(50) NOT NULL,
    expected_value TEXT,
    logical_operator VARCHAR(10) DEFAULT 'AND',
    condition_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    FOREIGN KEY (flow_route_id) REFERENCES flow_routes(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- Saga Transactions table
-- Add missing columns to existing tables if they don't exist
ALTER TABLE flow_routes ADD COLUMN IF NOT EXISTS condition_operator VARCHAR(10) DEFAULT 'AND';
ALTER TABLE route_conditions ADD COLUMN IF NOT EXISTS source_type VARCHAR(50);
ALTER TABLE route_conditions ADD COLUMN IF NOT EXISTS source_path VARCHAR(255);
ALTER TABLE saga_transactions ADD COLUMN IF NOT EXISTS correlation_id VARCHAR(255);

CREATE TABLE IF NOT EXISTS saga_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    saga_id VARCHAR(255) UNIQUE NOT NULL,
    flow_id UUID,
    transaction_type VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    current_step VARCHAR(255),
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,
    context_data TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    FOREIGN KEY (flow_id) REFERENCES integration_flows(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- Saga Steps table
CREATE TABLE IF NOT EXISTS saga_steps (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    saga_transaction_id UUID NOT NULL,
    step_name VARCHAR(255) NOT NULL,
    step_order INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    action_type VARCHAR(255) NOT NULL,
    action_data TEXT,
    compensation_type VARCHAR(255),
    compensation_data TEXT,
    result_data TEXT,
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,
    is_compensated BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    FOREIGN KEY (saga_transaction_id) REFERENCES saga_transactions(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- Messages table
CREATE TABLE IF NOT EXISTS messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id VARCHAR(255) UNIQUE NOT NULL,
    flow_id UUID,
    flow_execution_id UUID,
    status VARCHAR(50) NOT NULL,
    source_system VARCHAR(255),
    target_system VARCHAR(255),
    message_type VARCHAR(255),
    content_type VARCHAR(255),
    message_content TEXT,
    headers TEXT,
    properties TEXT,
    received_at TIMESTAMP NOT NULL,
    processed_at TIMESTAMP,
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,
    correlation_id VARCHAR(255),
    priority INTEGER DEFAULT 5,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    FOREIGN KEY (flow_id) REFERENCES integration_flows(id),
    FOREIGN KEY (flow_execution_id) REFERENCES flow_executions(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- Flow Executions table
CREATE TABLE IF NOT EXISTS flow_executions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    execution_id VARCHAR(255) UNIQUE NOT NULL,
    flow_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    trigger_type VARCHAR(50),
    triggered_by VARCHAR(255),
    execution_context TEXT,
    error_message TEXT,
    error_details TEXT,
    messages_processed INTEGER DEFAULT 0,
    messages_failed INTEGER DEFAULT 0,
    execution_time_ms BIGINT,
    current_step VARCHAR(255),
    retry_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    FOREIGN KEY (flow_id) REFERENCES integration_flows(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- Error Records table
CREATE TABLE IF NOT EXISTS error_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    error_id VARCHAR(255) UNIQUE NOT NULL,
    flow_id UUID,
    flow_execution_id UUID,
    message_id UUID,
    error_type VARCHAR(50) NOT NULL,
    error_code VARCHAR(100),
    error_message TEXT NOT NULL,
    stack_trace TEXT,
    component_name VARCHAR(255),
    occurred_at TIMESTAMP NOT NULL,
    severity VARCHAR(20) DEFAULT 'MEDIUM',
    is_resolved BOOLEAN DEFAULT false,
    resolved_at TIMESTAMP,
    resolved_by VARCHAR(255),
    resolution_notes TEXT,
    retry_count INTEGER DEFAULT 0,
    context_data TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    FOREIGN KEY (flow_id) REFERENCES integration_flows(id),
    FOREIGN KEY (flow_execution_id) REFERENCES flow_executions(id),
    FOREIGN KEY (message_id) REFERENCES messages(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- Retry Policies table
CREATE TABLE IF NOT EXISTS retry_policies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    policy_name VARCHAR(255) UNIQUE NOT NULL,
    flow_id UUID,
    error_type VARCHAR(50),
    max_attempts INTEGER DEFAULT 3,
    initial_interval_ms BIGINT DEFAULT 1000,
    multiplier DOUBLE PRECISION DEFAULT 2.0,
    max_interval_ms BIGINT DEFAULT 60000,
    retry_strategy VARCHAR(50) DEFAULT 'EXPONENTIAL_BACKOFF',
    is_active BOOLEAN DEFAULT true,
    retry_on_errors TEXT,
    skip_on_errors TEXT,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    FOREIGN KEY (flow_id) REFERENCES integration_flows(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- Dead Letter Messages table
CREATE TABLE IF NOT EXISTS dead_letter_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    original_message_id UUID,
    flow_id UUID,
    message_id VARCHAR(255) NOT NULL,
    message_content TEXT NOT NULL,
    headers TEXT,
    properties TEXT,
    error_reason TEXT NOT NULL,
    error_details TEXT,
    last_error_type VARCHAR(50),
    retry_count INTEGER DEFAULT 0,
    queued_at TIMESTAMP NOT NULL,
    last_retry_at TIMESTAMP,
    is_reprocessed BOOLEAN DEFAULT false,
    reprocessed_at TIMESTAMP,
    reprocessed_by VARCHAR(255),
    reprocess_result TEXT,
    correlation_id VARCHAR(255),
    source_system VARCHAR(255),
    target_system VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    FOREIGN KEY (original_message_id) REFERENCES messages(id),
    FOREIGN KEY (flow_id) REFERENCES integration_flows(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- Adapter Health Records table
CREATE TABLE IF NOT EXISTS adapter_health_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    adapter_id UUID NOT NULL,
    health_status VARCHAR(20) NOT NULL,
    checked_at TIMESTAMP NOT NULL,
    response_time_ms BIGINT,
    available_connections INTEGER,
    active_connections INTEGER,
    error_count INTEGER DEFAULT 0,
    success_count INTEGER DEFAULT 0,
    cpu_usage DOUBLE PRECISION,
    memory_usage DOUBLE PRECISION,
    last_error_message TEXT,
    health_details TEXT,
    is_available BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    FOREIGN KEY (adapter_id) REFERENCES communication_adapters(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- Flow Routers table
CREATE TABLE IF NOT EXISTS flow_routers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    router_name VARCHAR(255) UNIQUE NOT NULL,
    flow_id UUID,
    router_type VARCHAR(50) NOT NULL,
    configuration TEXT,
    is_active BOOLEAN DEFAULT true,
    description TEXT,
    input_channel VARCHAR(255),
    default_output_channel VARCHAR(255),
    evaluation_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    FOREIGN KEY (flow_id) REFERENCES integration_flows(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- Router Channel Mappings table
CREATE TABLE IF NOT EXISTS router_channel_mappings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    router_id UUID NOT NULL,
    condition_value VARCHAR(255),
    output_channel VARCHAR(255),
    FOREIGN KEY (router_id) REFERENCES flow_routers(id) ON DELETE CASCADE
);

-- Add indexes for better performance
CREATE INDEX idx_flow_routes_flow_id ON flow_routes(flow_id);
CREATE INDEX idx_route_conditions_flow_route_id ON route_conditions(flow_route_id);
CREATE INDEX idx_saga_transactions_flow_id ON saga_transactions(flow_id);
CREATE INDEX idx_saga_steps_saga_transaction_id ON saga_steps(saga_transaction_id);
CREATE INDEX idx_messages_flow_id ON messages(flow_id);
CREATE INDEX idx_messages_flow_execution_id ON messages(flow_execution_id);
CREATE INDEX idx_messages_status ON messages(status);
CREATE INDEX idx_flow_executions_flow_id ON flow_executions(flow_id);
CREATE INDEX idx_flow_executions_status ON flow_executions(status);
CREATE INDEX idx_error_records_flow_id ON error_records(flow_id);
CREATE INDEX idx_error_records_resolved ON error_records(is_resolved);
CREATE INDEX idx_dead_letter_messages_flow_id ON dead_letter_messages(flow_id);
CREATE INDEX idx_dead_letter_messages_reprocessed ON dead_letter_messages(is_reprocessed);
CREATE INDEX idx_adapter_health_records_adapter_id ON adapter_health_records(adapter_id);
CREATE INDEX idx_adapter_health_records_checked_at ON adapter_health_records(checked_at);
CREATE INDEX idx_flow_routers_flow_id ON flow_routers(flow_id);