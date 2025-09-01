-- Create external authentication table
CREATE TABLE IF NOT EXISTS external_authentications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    auth_type VARCHAR(20) NOT NULL CHECK (auth_type IN ('BASIC', 'OAUTH1', 'OAUTH2', 'API_KEY', 'CUSTOM')),
    business_component_id UUID NOT NULL REFERENCES business_components(id) ON DELETE CASCADE,
    
    -- Basic Auth fields
    username VARCHAR(255),
    encrypted_password TEXT,
    realm VARCHAR(100),
    
    -- OAuth 1.0 fields
    consumer_key VARCHAR(255),
    consumer_secret TEXT,
    oauth1_token VARCHAR(500),
    oauth1_token_secret TEXT,
    
    -- OAuth 2.0 fields
    client_id VARCHAR(255),
    encrypted_client_secret TEXT,
    token_endpoint VARCHAR(500),
    authorization_endpoint VARCHAR(500),
    redirect_uri VARCHAR(500),
    scopes TEXT,
    grant_type VARCHAR(50),
    encrypted_access_token TEXT,
    refresh_token TEXT,
    token_expires_at TIMESTAMP,
    
    -- API Key fields
    encrypted_api_key TEXT,
    api_key_header VARCHAR(100),
    api_key_prefix VARCHAR(50),
    
    -- Rate limiting
    rate_limit INTEGER,
    rate_limit_window_seconds INTEGER,
    
    -- Status fields
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_used_at TIMESTAMP,
    usage_count BIGINT DEFAULT 0,
    error_count BIGINT DEFAULT 0,
    
    -- Audit fields
    created_by_id UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Ensure unique name per business component
    CONSTRAINT uk_external_auth_name_component UNIQUE (name, business_component_id)
);

-- Create indexes for performance
CREATE INDEX idx_external_auth_name ON external_authentications(name);
CREATE INDEX idx_external_auth_type ON external_authentications(auth_type);
CREATE INDEX idx_external_auth_user ON external_authentications(created_by_id);
CREATE INDEX idx_external_auth_business_component ON external_authentications(business_component_id);
CREATE INDEX idx_external_auth_active_type ON external_authentications(is_active, auth_type);
CREATE INDEX idx_external_auth_token_expiry ON external_authentications(token_expires_at) WHERE auth_type = 'OAUTH2';

-- Create authentication attempt log table for auditing
CREATE TABLE IF NOT EXISTS external_auth_attempts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    external_auth_id UUID NOT NULL REFERENCES external_authentications(id) ON DELETE CASCADE,
    adapter_id UUID REFERENCES communication_adapters(id) ON DELETE SET NULL,
    flow_id UUID REFERENCES integration_flows(id) ON DELETE SET NULL,
    success BOOLEAN NOT NULL,
    status_code INTEGER,
    error_message TEXT,
    request_headers TEXT,
    response_headers TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    attempted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    duration_ms BIGINT
);

CREATE INDEX idx_auth_attempts_external_auth ON external_auth_attempts(external_auth_id);
CREATE INDEX idx_auth_attempts_timestamp ON external_auth_attempts(attempted_at);
CREATE INDEX idx_auth_attempts_success ON external_auth_attempts(success);

-- Add external_auth_id column to communication adapters
ALTER TABLE communication_adapters 
ADD COLUMN IF NOT EXISTS external_auth_id UUID REFERENCES external_authentications(id) ON DELETE SET NULL;

CREATE INDEX idx_adapters_external_auth ON communication_adapters(external_auth_id);

-- Comments for documentation
COMMENT ON TABLE external_authentications IS 'Stores external authentication configurations for API integrations';
COMMENT ON COLUMN external_authentications.auth_type IS 'Type of authentication: BASIC, OAUTH1, OAUTH2, API_KEY, CUSTOM';
COMMENT ON COLUMN external_authentications.encrypted_password IS 'Encrypted password for basic authentication';
COMMENT ON COLUMN external_authentications.encrypted_client_secret IS 'Encrypted OAuth2 client secret';
COMMENT ON COLUMN external_authentications.encrypted_access_token IS 'Encrypted OAuth2 access token';
COMMENT ON COLUMN external_authentications.encrypted_api_key IS 'Encrypted API key';
COMMENT ON COLUMN external_authentications.token_expires_at IS 'OAuth2 token expiration timestamp';
COMMENT ON COLUMN external_authentications.rate_limit IS 'Maximum requests allowed in rate limit window';
COMMENT ON COLUMN external_authentications.rate_limit_window_seconds IS 'Time window for rate limiting in seconds';

COMMENT ON TABLE external_auth_attempts IS 'Log of authentication attempts for monitoring and debugging';
COMMENT ON COLUMN communication_adapters.external_auth_id IS 'Reference to external authentication configuration';