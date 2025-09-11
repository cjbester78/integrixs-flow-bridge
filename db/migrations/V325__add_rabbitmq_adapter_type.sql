-- Add RABBITMQ adapter type to the enum
ALTER TYPE adapter_type ADD VALUE IF NOT EXISTS 'RABBITMQ' AFTER 'TEAMS';