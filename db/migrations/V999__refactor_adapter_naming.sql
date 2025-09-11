-- Forward Migration Script for Adapter Naming Refactoring
-- Changes INBOUND/OUTBOUND to INBOUND/OUTBOUND to align with industry standards

-- ============================================
-- PRE-MIGRATION CHECKS
-- ============================================

-- Ensure we have a backup
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.schemata WHERE schema_name = 'backup_before_refactoring') THEN
        RAISE WARNING 'Backup schema not found! Run backup_and_analysis.sql first.';
    END IF;
END $$;

-- ============================================
-- STEP 1: UPDATE ADAPTER MODE ENUM
-- ============================================

-- Add new enum values first (PostgreSQL doesn't allow direct rename in transaction)
ALTER TYPE adapter_mode_enum ADD VALUE IF NOT EXISTS 'INBOUND';
ALTER TYPE adapter_mode_enum ADD VALUE IF NOT EXISTS 'OUTBOUND';

-- Update existing data to use new values
UPDATE communication_adapters 
SET mode = 'INBOUND' 
WHERE mode = 'INBOUND';

UPDATE communication_adapters 
SET mode = 'OUTBOUND' 
WHERE mode = 'OUTBOUND';

-- Note: We'll need to handle removal of old enum values in a separate migration
-- as PostgreSQL doesn't support removing enum values easily

-- ============================================
-- STEP 2: UPDATE FLOW STRUCTURE DIRECTION
-- ============================================

-- Add new enum values for flow direction if using enum
-- If direction is text, just update the values
UPDATE flow_structures 
SET direction = CASE 
    WHEN direction = 'SOURCE' THEN 'INBOUND'
    WHEN direction = 'TARGET' THEN 'OUTBOUND'
    ELSE direction
END;

-- ============================================
-- STEP 3: RENAME COLUMNS
-- ============================================

-- Rename columns in integration_flows table
ALTER TABLE integration_flows 
    RENAME COLUMN inbound_adapter_id TO inbound_adapter_id;
ALTER TABLE integration_flows 
    RENAME COLUMN outbound_adapter_id TO outbound_adapter_id;

-- Update any foreign key constraints
ALTER TABLE integration_flows
    DROP CONSTRAINT IF EXISTS fk_integration_flows_source_adapter,
    DROP CONSTRAINT IF EXISTS fk_integration_flows_target_adapter;

ALTER TABLE integration_flows
    ADD CONSTRAINT fk_integration_flows_inbound_adapter 
    FOREIGN KEY (inbound_adapter_id) 
    REFERENCES communication_adapters(id),
    ADD CONSTRAINT fk_integration_flows_outbound_adapter 
    FOREIGN KEY (outbound_adapter_id) 
    REFERENCES communication_adapters(id);

-- ============================================
-- STEP 4: UPDATE JSON CONFIGURATIONS
-- ============================================

-- Update JSON configurations that contain mode references
UPDATE communication_adapters
SET configuration = 
    REPLACE(
        REPLACE(configuration::text, '"mode":"INBOUND"', '"mode":"INBOUND"'),
        '"mode":"OUTBOUND"', '"mode":"OUTBOUND"'
    )::json
WHERE configuration::text LIKE '%"mode":"INBOUND"%' 
   OR configuration::text LIKE '%"mode":"OUTBOUND"%';

-- ============================================
-- STEP 5: CREATE NEW ADAPTER MODE TYPE (CLEAN)
-- ============================================

-- Since we can't remove enum values, create a new clean enum type
-- This is optional but recommended for clean schema

/*
-- Create new enum with only new values
CREATE TYPE adapter_mode_enum_new AS ENUM ('INBOUND', 'OUTBOUND');

-- Update column to use new enum
ALTER TABLE communication_adapters 
    ALTER COLUMN mode TYPE adapter_mode_enum_new 
    USING mode::text::adapter_mode_enum_new;

-- Drop old enum
DROP TYPE adapter_mode_enum;

-- Rename new enum to original name
ALTER TYPE adapter_mode_enum_new RENAME TO adapter_mode_enum;
*/

-- ============================================
-- STEP 6: UPDATE INDEXES IF NEEDED
-- ============================================

-- Drop and recreate any indexes that might reference old column names
DROP INDEX IF EXISTS idx_integration_flows_source_adapter;
DROP INDEX IF EXISTS idx_integration_flows_target_adapter;

CREATE INDEX idx_integration_flows_inbound_adapter 
    ON integration_flows(inbound_adapter_id);
CREATE INDEX idx_integration_flows_outbound_adapter 
    ON integration_flows(outbound_adapter_id);

-- ============================================
-- STEP 7: UPDATE COMMENTS
-- ============================================

-- Update table and column comments
COMMENT ON COLUMN integration_flows.inbound_adapter_id IS 
    'Reference to the inbound adapter (receives data from external systems)';
COMMENT ON COLUMN integration_flows.outbound_adapter_id IS 
    'Reference to the outbound adapter (sends data to external systems)';

COMMENT ON COLUMN communication_adapters.mode IS 
    'Adapter mode: INBOUND (receives from external) or OUTBOUND (sends to external)';

-- ============================================
-- STEP 8: CREATE COMPATIBILITY VIEWS (OPTIONAL)
-- ============================================

-- Create views with old column names for backward compatibility
CREATE OR REPLACE VIEW v_integration_flows_legacy AS
SELECT 
    id,
    name,
    description,
    inbound_adapter_id as inbound_adapter_id,
    outbound_adapter_id as outbound_adapter_id,
    transformation_id,
    status,
    created_at,
    updated_at
FROM integration_flows;

-- ============================================
-- VERIFICATION QUERIES
-- ============================================

-- Verify adapter modes have been updated
SELECT 
    mode,
    COUNT(*) as count
FROM communication_adapters
GROUP BY mode
ORDER BY mode;

-- Verify flow structures
SELECT 
    direction,
    COUNT(*) as count
FROM flow_structures
GROUP BY direction
ORDER BY direction;

-- Verify column renames
SELECT 
    column_name,
    data_type
FROM information_schema.columns
WHERE table_name = 'integration_flows'
AND column_name IN ('inbound_adapter_id', 'outbound_adapter_id')
ORDER BY ordinal_position;

-- Check for any remaining old terminology in JSON
SELECT 
    COUNT(*) as configs_with_old_terminology
FROM communication_adapters
WHERE configuration::text LIKE '%INBOUND%'
   OR configuration::text LIKE '%OUTBOUND%';

-- ============================================
-- POST-MIGRATION REPORT
-- ============================================

WITH migration_stats AS (
    SELECT 
        (SELECT COUNT(*) FROM communication_adapters WHERE mode = 'INBOUND') as inbound_count,
        (SELECT COUNT(*) FROM communication_adapters WHERE mode = 'OUTBOUND') as outbound_count,
        (SELECT COUNT(*) FROM flow_structures WHERE direction = 'INBOUND') as inbound_structures,
        (SELECT COUNT(*) FROM flow_structures WHERE direction = 'OUTBOUND') as outbound_structures,
        (SELECT COUNT(*) FROM integration_flows WHERE inbound_adapter_id IS NOT NULL) as flows_with_inbound,
        (SELECT COUNT(*) FROM integration_flows WHERE outbound_adapter_id IS NOT NULL) as flows_with_outbound
)
SELECT 
    'Migration completed successfully!' as status,
    inbound_count || ' inbound adapters' as inbound_adapters,
    outbound_count || ' outbound adapters' as outbound_adapters,
    inbound_structures || ' inbound flow structures' as flow_structures_in,
    outbound_structures || ' outbound flow structures' as flow_structures_out,
    flows_with_inbound || ' flows with inbound adapter' as flows_in,
    flows_with_outbound || ' flows with outbound adapter' as flows_out
FROM migration_stats;