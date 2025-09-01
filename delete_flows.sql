-- Delete flows and related data
-- This script will delete integration flows and their associated mappings

-- First, show what we're going to delete
SELECT 'Flows to delete:' as info;
SELECT id, name, status, deployment_endpoint FROM integration_flows WHERE status = 'DEPLOYED_ACTIVE' OR name LIKE '%Test%' OR name LIKE '%SOAP%';

-- Get the flow IDs we need to delete
SET @flow_ids = (SELECT GROUP_CONCAT(id) FROM integration_flows WHERE status = 'DEPLOYED_ACTIVE' OR name LIKE '%Test%' OR name LIKE '%SOAP%');

-- Delete field mappings first (they reference transformations)
DELETE fm FROM field_mappings fm
INNER JOIN flow_transformations ft ON fm.transformation_id = ft.id
WHERE FIND_IN_SET(ft.flow_id, @flow_ids);

-- Delete flow transformations
DELETE FROM flow_transformations WHERE FIND_IN_SET(flow_id, @flow_ids);

-- Delete the flows themselves
DELETE FROM integration_flows WHERE status = 'DEPLOYED_ACTIVE' OR name LIKE '%Test%' OR name LIKE '%SOAP%';

-- Verify deletion
SELECT 'Remaining flows:' as info;
SELECT COUNT(*) as remaining_flows FROM integration_flows;
SELECT 'Remaining transformations:' as info;
SELECT COUNT(*) as remaining_transformations FROM flow_transformations;
SELECT 'Remaining field mappings:' as info;
SELECT COUNT(*) as remaining_mappings FROM field_mappings;