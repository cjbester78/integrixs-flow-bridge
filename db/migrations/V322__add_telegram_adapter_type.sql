-- Add TELEGRAM adapter type to the enum
ALTER TYPE adapter_type ADD VALUE IF NOT EXISTS 'TELEGRAM' AFTER 'DISCORD';