-- Enhance field_mappings table to support 1-to-many field mappings

-- Add new columns for enhanced field mapping support
ALTER TABLE field_mappings ADD COLUMN IF NOT EXISTS target_fields JSON;
ALTER TABLE field_mappings ADD COLUMN IF NOT EXISTS mapping_type VARCHAR(50) DEFAULT 'DIRECT';
ALTER TABLE field_mappings ADD COLUMN IF NOT EXISTS split_configuration JSON;

-- Make target_field nullable for new entries using target_fields
ALTER TABLE field_mappings ALTER COLUMN target_field DROP NOT NULL;

-- Add check constraint to ensure at least one target is specified
ALTER TABLE field_mappings ADD CONSTRAINT chk_field_mapping_target 
    CHECK (target_field IS NOT NULL OR target_fields IS NOT NULL);

-- Add check constraint for valid mapping types
ALTER TABLE field_mappings ADD CONSTRAINT chk_mapping_type 
    CHECK (mapping_type IN ('DIRECT', 'SPLIT', 'AGGREGATE', 'CONDITIONAL', 'ITERATE'));

-- Create index on mapping_type for performance
CREATE INDEX IF NOT EXISTS idx_field_mapping_type ON field_mappings(mapping_type);

-- Add comments
COMMENT ON COLUMN field_mappings.target_fields IS 'JSON array of target field names for 1-to-many mappings';
COMMENT ON COLUMN field_mappings.mapping_type IS 'Type of field mapping: DIRECT (1-to-1 or many-to-1), SPLIT (1-to-many), AGGREGATE (many-to-many), CONDITIONAL, ITERATE';
COMMENT ON COLUMN field_mappings.split_configuration IS 'Configuration for SPLIT type mappings defining how to split source value to multiple targets';

-- Migrate existing data to new structure
UPDATE field_mappings 
SET target_fields = jsonb_build_array(target_field)::json 
WHERE target_field IS NOT NULL AND target_fields IS NULL;

-- Example split configurations:
-- For string splitting: {"method": "split", "delimiter": ",", "trim": true}
-- For array distribution: {"method": "index", "indices": [0, 1, 2]}
-- For value duplication: {"method": "duplicate"}
-- For calculated split: {"method": "expression", "expressions": ["substring(0,5)", "substring(5,10)"]}