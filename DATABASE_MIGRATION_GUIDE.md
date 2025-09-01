# Database Migration Guide for Adapter Naming Refactoring

## Overview
This guide provides step-by-step instructions for safely executing the database migration to update adapter naming from SENDER/RECEIVER to INBOUND/OUTBOUND.

## Pre-Migration Checklist

- [ ] All application instances stopped or in maintenance mode
- [ ] Database backup completed
- [ ] Migration scripts reviewed
- [ ] Rollback procedure understood
- [ ] Team notified of migration window

## Migration Steps

### Step 1: Connect to Database
```bash
psql -h localhost -U your_user -d your_database
```

### Step 2: Run Backup and Analysis Script
```sql
\i database/migrations/backup_and_analysis.sql
```

This will:
- Create backup schema `backup_before_refactoring`
- Copy all relevant tables
- Generate analysis report

### Step 3: Verify Backup
```sql
-- Check backup tables exist
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'backup_before_refactoring'
ORDER BY table_name;

-- Verify row counts match
SELECT 
    'communication_adapters' as table_name,
    (SELECT COUNT(*) FROM public.communication_adapters) as original,
    (SELECT COUNT(*) FROM backup_before_refactoring.communication_adapters) as backup
UNION ALL
SELECT 
    'integration_flows',
    (SELECT COUNT(*) FROM public.integration_flows),
    (SELECT COUNT(*) FROM backup_before_refactoring.integration_flows)
UNION ALL
SELECT 
    'flow_structures',
    (SELECT COUNT(*) FROM public.flow_structures),
    (SELECT COUNT(*) FROM backup_before_refactoring.flow_structures);
```

### Step 4: Execute Migration
```sql
\i database/migrations/V999__refactor_adapter_naming.sql
```

This migration will:
1. Update adapter_mode_enum values (SENDER → INBOUND, RECEIVER → OUTBOUND)
2. Update flow_structures direction values
3. Rename columns in integration_flows table
4. Update JSON configurations
5. Create compatibility views

### Step 5: Verify Migration Success
```sql
-- Check adapter modes
SELECT mode, COUNT(*) 
FROM communication_adapters 
GROUP BY mode;

-- Expected output:
-- INBOUND  | count
-- OUTBOUND | count

-- Check column names
SELECT column_name 
FROM information_schema.columns 
WHERE table_name = 'integration_flows' 
AND column_name IN ('inbound_adapter_id', 'outbound_adapter_id');

-- Check for any remaining old terminology
SELECT COUNT(*) as old_terminology_count
FROM communication_adapters
WHERE configuration::text LIKE '%SENDER%' 
   OR configuration::text LIKE '%RECEIVER%';
-- Should return 0
```

### Step 6: Test Application Connectivity
Before starting the application:

1. **Test a simple adapter query:**
```sql
SELECT id, name, adapter_type, mode 
FROM communication_adapters 
LIMIT 5;
```

2. **Test a flow query:**
```sql
SELECT f.id, f.name, 
       ca1.name as inbound_adapter,
       ca2.name as outbound_adapter
FROM integration_flows f
LEFT JOIN communication_adapters ca1 ON f.inbound_adapter_id = ca1.id
LEFT JOIN communication_adapters ca2 ON f.outbound_adapter_id = ca2.id
LIMIT 5;
```

## Rollback Procedure (If Needed)

### Option 1: Quick Rollback (Recommended)
```sql
\i database/migrations/rollback_adapter_naming.sql
```

### Option 2: Full Restore from Backup
```sql
-- WARNING: This will lose any changes made after backup
BEGIN;

-- Restore tables
TRUNCATE TABLE public.communication_adapters CASCADE;
INSERT INTO public.communication_adapters
SELECT * FROM backup_before_refactoring.communication_adapters;

TRUNCATE TABLE public.integration_flows CASCADE;
INSERT INTO public.integration_flows
SELECT * FROM backup_before_refactoring.integration_flows;

TRUNCATE TABLE public.flow_structures CASCADE;
INSERT INTO public.flow_structures
SELECT * FROM backup_before_refactoring.flow_structures;

COMMIT;
```

## Post-Migration Tasks

### 1. Start Application in Test Mode
- Start one instance first
- Monitor logs for any errors
- Test basic adapter operations

### 2. Validate Core Functionality
- [ ] Create a test adapter (both inbound and outbound)
- [ ] Create a test flow
- [ ] Execute the test flow
- [ ] Check monitoring data

### 3. Performance Check
```sql
-- Check for any slow queries
SELECT query, calls, mean_time 
FROM pg_stat_statements 
WHERE query LIKE '%adapter%' 
ORDER BY mean_time DESC 
LIMIT 10;
```

### 4. Clean Up (After Validation)
Once everything is confirmed working (recommend waiting 1-2 weeks):
```sql
-- Drop backup schema
DROP SCHEMA backup_before_refactoring CASCADE;

-- Drop compatibility views if not needed
DROP VIEW IF EXISTS v_integration_flows_legacy;
```

## Troubleshooting

### Issue: Enum value already exists
```sql
-- Check existing enum values
SELECT enumlabel 
FROM pg_enum 
WHERE enumtypid = (SELECT oid FROM pg_type WHERE typname = 'adapter_mode_enum');
```

### Issue: Foreign key constraint violation
```sql
-- Check for orphaned references
SELECT f.* 
FROM integration_flows f
WHERE f.inbound_adapter_id NOT IN (SELECT id FROM communication_adapters)
   OR f.outbound_adapter_id NOT IN (SELECT id FROM communication_adapters);
```

### Issue: Application can't connect after migration
1. Check connection pool settings
2. Clear application cache
3. Restart application with fresh connection pool

## Migration Timeline Estimate
- Backup and analysis: 5-10 minutes
- Migration execution: 2-5 minutes
- Verification: 5-10 minutes
- Application restart: 5-10 minutes
- **Total downtime: 20-35 minutes**

## Emergency Contacts
- Database Admin: [Contact info]
- Application Team Lead: [Contact info]
- On-call Engineer: [Contact info]

---

**Remember**: Take your time, verify each step, and don't hesitate to rollback if anything seems wrong.