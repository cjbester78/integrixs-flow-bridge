-- Create flow_orchestration_steps table for storing orchestration flow steps
CREATE TABLE IF NOT EXISTS flow_orchestration_steps (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    flow_id UUID NOT NULL,
    step_type VARCHAR(50) NOT NULL,
    step_name VARCHAR(255),
    description TEXT,
    execution_order INTEGER NOT NULL,
    configuration JSONB,
    condition_expression TEXT,
    is_conditional BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    timeout_seconds INTEGER,
    retry_attempts INTEGER DEFAULT 0,
    retry_delay_seconds INTEGER DEFAULT 60,
    target_adapter_id UUID,
    target_flow_structure_id UUID,
    transformation_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_orchestration_flow
        FOREIGN KEY (flow_id) 
        REFERENCES integration_flows(id) 
        ON DELETE CASCADE,
        
    CONSTRAINT fk_target_adapter
        FOREIGN KEY (target_adapter_id)
        REFERENCES communication_adapters(id)
        ON DELETE SET NULL,
        
    CONSTRAINT fk_target_flow_structure
        FOREIGN KEY (target_flow_structure_id)
        REFERENCES flow_structures(id)
        ON DELETE SET NULL,
        
    CONSTRAINT fk_transformation
        FOREIGN KEY (transformation_id)
        REFERENCES flow_transformations(id)
        ON DELETE SET NULL
);

-- Create indexes for better query performance
CREATE INDEX idx_orchestration_flow_id ON flow_orchestration_steps(flow_id);
CREATE INDEX idx_orchestration_execution_order ON flow_orchestration_steps(flow_id, execution_order);
CREATE INDEX idx_orchestration_active ON flow_orchestration_steps(flow_id, is_active);
CREATE INDEX idx_orchestration_step_type ON flow_orchestration_steps(step_type);

-- Add trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_flow_orchestration_steps_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_flow_orchestration_steps_updated_at_trigger
    BEFORE UPDATE ON flow_orchestration_steps
    FOR EACH ROW
    EXECUTE FUNCTION update_flow_orchestration_steps_updated_at();

-- Add comment to table
COMMENT ON TABLE flow_orchestration_steps IS 'Stores individual steps for orchestration flows with their configuration and execution order';
COMMENT ON COLUMN flow_orchestration_steps.step_type IS 'Type of orchestration step: ROUTE, TRANSFORM, CONDITION, LOOP, AGGREGATE, SPLIT, ENRICH, VALIDATE, LOG, CUSTOM';
COMMENT ON COLUMN flow_orchestration_steps.configuration IS 'JSON configuration specific to the step type';
COMMENT ON COLUMN flow_orchestration_steps.condition_expression IS 'Expression for conditional steps (e.g., SpEL expression)';
COMMENT ON COLUMN flow_orchestration_steps.target_adapter_id IS 'For ROUTE steps - the target adapter to route to';
COMMENT ON COLUMN flow_orchestration_steps.target_flow_structure_id IS 'For ROUTE steps - the target flow structure';
COMMENT ON COLUMN flow_orchestration_steps.transformation_id IS 'For TRANSFORM steps - the transformation to apply';