-- Create orchestration_targets table
CREATE TABLE IF NOT EXISTS orchestration_targets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    flow_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    target_type VARCHAR(50) NOT NULL,
    target_endpoint VARCHAR(500),
    authentication_type VARCHAR(50),
    authentication_config JSONB,
    request_template TEXT,
    response_template TEXT,
    error_handling_strategy VARCHAR(50),
    retry_config JSONB,
    timeout_seconds INTEGER DEFAULT 30,
    is_active BOOLEAN DEFAULT true,
    execution_order INTEGER DEFAULT 0,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_orchestration_target_flow 
        FOREIGN KEY (flow_id) 
        REFERENCES integration_flows(id) 
        ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_orchestration_targets_flow_id ON orchestration_targets(flow_id);
CREATE INDEX idx_orchestration_targets_is_active ON orchestration_targets(is_active);
CREATE INDEX idx_orchestration_targets_execution_order ON orchestration_targets(execution_order);

-- Add trigger to update updated_at timestamp
CREATE TRIGGER update_orchestration_targets_updated_at
    BEFORE UPDATE ON orchestration_targets
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();