-- Database Backup and Analysis Script for Adapter Naming Refactoring
-- Run this script before starting the refactoring process

-- ============================================
-- STEP 1: CREATE BACKUP SCHEMA
-- ============================================
CREATE SCHEMA IF NOT EXISTS backup_before_refactoring;

-- ============================================
-- STEP 2: BACKUP CRITICAL TABLES
-- ============================================

-- Backup communication_adapters table
CREATE TABLE backup_before_refactoring.communication_adapters AS
SELECT * FROM public.communication_adapters;

-- Backup integration_flows table
CREATE TABLE backup_before_refactoring.integration_flows AS
SELECT * FROM public.integration_flows;

-- Backup flow_structures table
CREATE TABLE backup_before_refactoring.flow_structures AS
SELECT * FROM public.flow_structures;

-- Backup flow_executions table (for reference)
CREATE TABLE backup_before_refactoring.flow_executions AS
SELECT * FROM public.flow_executions;

-- ============================================
-- STEP 3: ANALYSIS QUERIES
-- ============================================

-- Count adapters by mode
SELECT 
    mode,
    COUNT(*) as count,
    COUNT(DISTINCT adapter_type) as unique_types
FROM communication_adapters
GROUP BY mode
ORDER BY mode;

-- List all adapter types and their modes
SELECT DISTINCT
    adapter_type,
    mode,
    COUNT(*) as count
FROM communication_adapters
GROUP BY adapter_type, mode
ORDER BY adapter_type, mode;

-- Check for active flows using each adapter mode
SELECT 
    ca.mode,
    COUNT(DISTINCT if.id) as active_flows
FROM integration_flows if
JOIN communication_adapters ca ON ca.id IN (if.inbound_adapter_id, if.outbound_adapter_id)
WHERE if.status = 'ACTIVE'
GROUP BY ca.mode;

-- Analyze flow structures direction usage
SELECT 
    direction,
    COUNT(*) as count
FROM flow_structures
GROUP BY direction;

-- Find JSON configurations containing mode references
SELECT 
    id,
    name,
    adapter_type,
    mode,
    configuration::text as config_text
FROM communication_adapters
WHERE configuration::text LIKE '%"mode"%'
LIMIT 10;

-- Check for any custom SQL or stored procedures that might reference these enums
SELECT 
    routine_name,
    routine_type,
    routine_definition
FROM information_schema.routines
WHERE routine_schema = 'public'
AND (
    routine_definition ILIKE '%sender%'
    OR routine_definition ILIKE '%receiver%'
    OR routine_definition ILIKE '%source_adapter%'
    OR routine_definition ILIKE '%target_adapter%'
);

-- Check for any views that might need updating
SELECT 
    table_name,
    view_definition
FROM information_schema.views
WHERE table_schema = 'public'
AND (
    view_definition ILIKE '%sender%'
    OR view_definition ILIKE '%receiver%'
    OR view_definition ILIKE '%source_adapter%'
    OR view_definition ILIKE '%target_adapter%'
);

-- Check for any triggers
SELECT 
    trigger_name,
    event_object_table,
    action_statement
FROM information_schema.triggers
WHERE trigger_schema = 'public';

-- ============================================
-- STEP 4: GENERATE STATISTICS REPORT
-- ============================================

-- Summary statistics
WITH stats AS (
    SELECT 
        (SELECT COUNT(*) FROM communication_adapters) as total_adapters,
        (SELECT COUNT(*) FROM communication_adapters WHERE mode = 'INBOUND') as sender_count,
        (SELECT COUNT(*) FROM communication_adapters WHERE mode = 'OUTBOUND') as receiver_count,
        (SELECT COUNT(*) FROM integration_flows) as total_flows,
        (SELECT COUNT(*) FROM integration_flows WHERE status = 'ACTIVE') as active_flows,
        (SELECT COUNT(*) FROM flow_executions WHERE created_at > NOW() - INTERVAL '7 days') as recent_executions
)
SELECT 
    'Total Adapters' as metric,
    total_adapters as value
FROM stats
UNION ALL
SELECT 'Sender Adapters', sender_count FROM stats
UNION ALL
SELECT 'Receiver Adapters', receiver_count FROM stats
UNION ALL
SELECT 'Total Flows', total_flows FROM stats
UNION ALL
SELECT 'Active Flows', active_flows FROM stats
UNION ALL
SELECT 'Recent Executions (7 days)', recent_executions FROM stats;

-- ============================================
-- STEP 5: EXPORT COMMANDS (Run from psql or pgAdmin)
-- ============================================

-- Full database backup command (run from command line):
-- pg_dump -h localhost -U your_user -d your_database -F c -b -v -f integrix_backup_before_refactoring.dump

-- Backup specific tables only:
-- pg_dump -h localhost -U your_user -d your_database -t communication_adapters -t integration_flows -t flow_structures -F c -b -v -f adapter_tables_backup.dump

-- ============================================
-- VERIFICATION QUERIES
-- ============================================

-- Verify backup was created successfully
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size
FROM pg_tables
WHERE schemaname = 'backup_before_refactoring'
ORDER BY tablename;

-- Compare row counts between original and backup
WITH original_counts AS (
    SELECT 'communication_adapters' as table_name, COUNT(*) as count FROM public.communication_adapters
    UNION ALL
    SELECT 'integration_flows', COUNT(*) FROM public.integration_flows
    UNION ALL
    SELECT 'flow_structures', COUNT(*) FROM public.flow_structures
),
backup_counts AS (
    SELECT 'communication_adapters' as table_name, COUNT(*) as count FROM backup_before_refactoring.communication_adapters
    UNION ALL
    SELECT 'integration_flows', COUNT(*) FROM backup_before_refactoring.integration_flows
    UNION ALL
    SELECT 'flow_structures', COUNT(*) FROM backup_before_refactoring.flow_structures
)
SELECT 
    o.table_name,
    o.count as original_count,
    b.count as backup_count,
    CASE WHEN o.count = b.count THEN 'OK' ELSE 'MISMATCH!' END as status
FROM original_counts o
JOIN backup_counts b ON o.table_name = b.table_name;