-- Fix created_by and updated_by columns to be UUID instead of VARCHAR
-- This allows proper JOINs with the users table

ALTER TABLE orchestration_targets 
ALTER COLUMN created_by TYPE UUID USING created_by::UUID,
ALTER COLUMN updated_by TYPE UUID USING updated_by::UUID;

-- Add foreign key constraints
ALTER TABLE orchestration_targets 
ADD CONSTRAINT fk_orchestration_targets_created_by FOREIGN KEY (created_by) REFERENCES users(id),
ADD CONSTRAINT fk_orchestration_targets_updated_by FOREIGN KEY (updated_by) REFERENCES users(id);