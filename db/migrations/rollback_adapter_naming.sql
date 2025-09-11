-- Rollback Script for Adapter Naming Refactoring
-- Use this script to revert changes if needed

-- ============================================
-- ROLLBACK PREPARATION
-- ============================================

-- First, verify backup exists
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.schemata WHERE schema_name = 'backup_before_refactoring') THEN
        RAISE EXCEPTION 'Backup schema not found! Cannot proceed with rollback.';
    END IF;
END $$;

-- ============================================
-- STEP 1: REVERT ENUM CHANGES
-- ============================================

-- Revert adapter_mode_enum back to INBOUND/OUTBOUND
BEGIN;
    -- First, update any INBOUND/OUTBOUND values back to INBOUND/OUTBOUND
    UPDATE communication_adapters 
    SET mode = 'INBOUND' 
    WHERE mode = 'INBOUND';
    
    UPDATE communication_adapters 
    SET mode = 'OUTBOUND' 
    WHERE mode = 'OUTBOUND';
    
    -- Rename enum values back
    ALTER TYPE adapter_mode_enum RENAME VALUE 'INBOUND' TO 'INBOUND';
    ALTER TYPE adapter_mode_enum RENAME VALUE 'OUTBOUND' TO 'OUTBOUND';
COMMIT;

-- ============================================
-- STEP 2: REVERT FLOW STRUCTURE DIRECTION
-- ============================================

-- Revert flow_structures direction column
UPDATE flow_structures 
SET direction = CASE 
    WHEN direction = 'INBOUND' THEN 'SOURCE'
    WHEN direction = 'OUTBOUND' THEN 'TARGET'
    ELSE direction
END;

-- ============================================
-- STEP 3: REVERT COLUMN NAMES
-- ============================================

-- Revert integration_flows column names
ALTER TABLE integration_flows 
    RENAME COLUMN inbound_adapter_id TO inbound_adapter_id;
ALTER TABLE integration_flows 
    RENAME COLUMN outbound_adapter_id TO outbound_adapter_id;

-- ============================================
-- STEP 4: REVERT JSON CONFIGURATIONS
-- ============================================

-- Revert JSON configurations that might contain mode references
UPDATE communication_adapters
SET configuration = 
    REPLACE(
        REPLACE(configuration::text, '"mode":"INBOUND"', '"mode":"INBOUND"'),
        '"mode":"OUTBOUND"', '"mode":"OUTBOUND"'
    )::json
WHERE configuration::text LIKE '%"mode":"INBOUND"%' 
   OR configuration::text LIKE '%"mode":"OUTBOUND"%';

-- ============================================
-- STEP 5: VERIFICATION QUERIES
-- ============================================

-- Verify enum values are back to original
SELECT enumlabel 
FROM pg_enum 
WHERE enumtypid = (SELECT oid FROM pg_type WHERE typname = 'adapter_mode_enum')
ORDER BY enumsortorder;

-- Verify adapter modes
SELECT mode, COUNT(*) 
FROM communication_adapters 
GROUP BY mode;

-- Verify column names
SELECT column_name 
FROM information_schema.columns 
WHERE table_name = 'integration_flows' 
AND column_name IN ('inbound_adapter_id', 'outbound_adapter_id', 'inbound_adapter_id', 'outbound_adapter_id');

-- ============================================
-- STEP 6: RESTORE FROM BACKUP (IF NEEDED)
-- ============================================

-- If rollback fails, restore tables from backup
-- WARNING: This will lose any changes made after the backup!

/*
-- Restore communication_adapters
TRUNCATE TABLE public.communication_adapters CASCADE;
INSERT INTO public.communication_adapters
SELECT * FROM backup_before_refactoring.communication_adapters;

-- Restore integration_flows
TRUNCATE TABLE public.integration_flows CASCADE;
INSERT INTO public.integration_flows
SELECT * FROM backup_before_refactoring.integration_flows;

-- Restore flow_structures
TRUNCATE TABLE public.flow_structures CASCADE;
INSERT INTO public.flow_structures
SELECT * FROM backup_before_refactoring.flow_structures;
*/

-- ============================================
-- STEP 7: CLEANUP (OPTIONAL)
-- ============================================

-- After successful rollback and verification, you can drop the backup schema
-- DROP SCHEMA backup_before_refactoring CASCADE;

-- ============================================
-- POST-ROLLBACK CHECKLIST
-- ============================================

/*
1. Run verification queries to ensure data integrity
2. Test a few adapters to ensure they work correctly
3. Check active flows are still functional
4. Update application code to use old terminology
5. Clear any caches that might have new terminology
6. Notify team of rollback completion
*/

-- Final verification query
SELECT 
    'Adapters with INBOUND mode' as check_item,
    COUNT(*) as count
FROM communication_adapters
WHERE mode = 'INBOUND'
UNION ALL
SELECT 
    'Adapters with OUTBOUND mode',
    COUNT(*)
FROM communication_adapters
WHERE mode = 'OUTBOUND'
UNION ALL
SELECT 
    'Flows with inbound_adapter_id',
    COUNT(*)
FROM integration_flows
WHERE inbound_adapter_id IS NOT NULL
UNION ALL
SELECT 
    'Flows with outbound_adapter_id',
    COUNT(*)
FROM integration_flows
WHERE outbound_adapter_id IS NOT NULL;