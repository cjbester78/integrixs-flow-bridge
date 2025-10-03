-- Add missing foreign key columns to orchestration_targets table
-- These columns are needed for the OrchestrationTarget entity relationships

ALTER TABLE orchestration_targets 
ADD COLUMN target_adapter_id UUID REFERENCES communication_adapters(id),
ADD COLUMN target_flow_id UUID REFERENCES integration_flows(id);

-- Add indexes for better query performance
CREATE INDEX idx_orchestration_targets_target_adapter_id ON orchestration_targets(target_adapter_id);
CREATE INDEX idx_orchestration_targets_target_flow_id ON orchestration_targets(target_flow_id);

-- Add constraint to ensure either target_adapter_id or target_flow_id is set (but not both)
ALTER TABLE orchestration_targets 
ADD CONSTRAINT chk_orchestration_target_exclusive 
CHECK (
    (target_adapter_id IS NOT NULL AND target_flow_id IS NULL) OR 
    (target_adapter_id IS NULL AND target_flow_id IS NOT NULL)
);