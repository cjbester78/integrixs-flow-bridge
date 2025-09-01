-- =====================================================
-- Migration: Add IP Whitelist Management
-- Description: Creates tables for managing IP whitelist
-- Author: Integration Team
-- Date: 2025-09-01
-- =====================================================

-- IP Whitelist table
CREATE TABLE IF NOT EXISTS ip_whitelist (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ip_address VARCHAR(45) NOT NULL,
    description TEXT,
    is_range BOOLEAN DEFAULT FALSE,
    added_by VARCHAR(100) NOT NULL,
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Add unique constraint on IP address
ALTER TABLE ip_whitelist ADD CONSTRAINT uk_ip_address UNIQUE (ip_address);

-- Add indexes
CREATE INDEX idx_ip_whitelist_active ON ip_whitelist(is_active);
CREATE INDEX idx_ip_whitelist_expires ON ip_whitelist(expires_at);
CREATE INDEX idx_ip_whitelist_added_by ON ip_whitelist(added_by);

-- IP Whitelist audit log
CREATE TABLE IF NOT EXISTS ip_whitelist_audit (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ip_address VARCHAR(45) NOT NULL,
    action VARCHAR(20) NOT NULL, -- ADD, REMOVE, UPDATE, EXTEND
    performed_by VARCHAR(100) NOT NULL,
    performed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    details JSONB,
    client_ip VARCHAR(45),
    user_agent TEXT
);

-- Add index on audit timestamp
CREATE INDEX idx_ip_whitelist_audit_timestamp ON ip_whitelist_audit(performed_at DESC);

-- Add comments
COMMENT ON TABLE ip_whitelist IS 'Stores whitelisted IP addresses and CIDR ranges for access control';
COMMENT ON COLUMN ip_whitelist.ip_address IS 'IP address or CIDR range (e.g., 192.168.1.0/24)';
COMMENT ON COLUMN ip_whitelist.is_range IS 'TRUE if this is a CIDR range, FALSE for single IP';
COMMENT ON COLUMN ip_whitelist.expires_at IS 'Optional expiration timestamp for temporary whitelist entries';

COMMENT ON TABLE ip_whitelist_audit IS 'Audit log for IP whitelist changes';
COMMENT ON COLUMN ip_whitelist_audit.action IS 'Type of action performed (ADD, REMOVE, UPDATE, EXTEND)';
COMMENT ON COLUMN ip_whitelist_audit.details IS 'Additional details about the action in JSON format';

-- Insert default localhost entries
INSERT INTO ip_whitelist (ip_address, description, is_range, added_by, is_active) 
VALUES 
    ('127.0.0.1', 'IPv4 Localhost', FALSE, 'SYSTEM', TRUE),
    ('::1', 'IPv6 Localhost', FALSE, 'SYSTEM', TRUE),
    ('10.0.0.0/8', 'Private Network Class A', TRUE, 'SYSTEM', TRUE),
    ('172.16.0.0/12', 'Private Network Class B', TRUE, 'SYSTEM', TRUE),
    ('192.168.0.0/16', 'Private Network Class C', TRUE, 'SYSTEM', TRUE)
ON CONFLICT (ip_address) DO NOTHING;