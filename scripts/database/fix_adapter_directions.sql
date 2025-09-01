-- Fix adapter directions to follow the reversed middleware convention
-- INBOUND = OUTBOUND (receives from external systems)
-- OUTBOUND = INBOUND (sends to external systems)

-- Update all INBOUND adapters to have OUTBOUND direction
UPDATE communication_adapters 
SET direction = 'OUTBOUND' 
WHERE mode = 'INBOUND';

-- Update all OUTBOUND adapters to have INBOUND direction  
UPDATE communication_adapters 
SET direction = 'INBOUND' 
WHERE mode = 'OUTBOUND';

-- Show the results
SELECT id, name, type, mode, direction 
FROM communication_adapters 
ORDER BY created_at;