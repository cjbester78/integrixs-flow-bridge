-- Add new authentication type fields for HMAC, Certificate, and Custom Header authentication

-- Add HMAC authentication fields
ALTER TABLE external_authentications ADD COLUMN hmac_algorithm VARCHAR(50);
ALTER TABLE external_authentications ADD COLUMN hmac_secret_key TEXT;
ALTER TABLE external_authentications ADD COLUMN hmac_header_name VARCHAR(100);
ALTER TABLE external_authentications ADD COLUMN hmac_include_timestamp BOOLEAN DEFAULT FALSE;
ALTER TABLE external_authentications ADD COLUMN hmac_include_nonce BOOLEAN DEFAULT FALSE;

-- Add Certificate authentication fields
ALTER TABLE external_authentications ADD COLUMN certificate_path VARCHAR(500);
ALTER TABLE external_authentications ADD COLUMN certificate_password TEXT;
ALTER TABLE external_authentications ADD COLUMN certificate_type VARCHAR(50);
ALTER TABLE external_authentications ADD COLUMN trust_store_path VARCHAR(500);
ALTER TABLE external_authentications ADD COLUMN trust_store_password TEXT;

-- Create table for custom headers
CREATE TABLE external_auth_custom_headers (
    auth_id UUID NOT NULL REFERENCES external_authentications(id) ON DELETE CASCADE,
    header_name VARCHAR(100) NOT NULL,
    header_value TEXT,
    PRIMARY KEY (auth_id, header_name)
);

-- Create index for custom headers
CREATE INDEX idx_external_auth_custom_headers_auth_id ON external_auth_custom_headers(auth_id);

-- Update auth_type constraint to include new types
ALTER TABLE external_authentications DROP CONSTRAINT IF EXISTS external_authentications_auth_type_check;
ALTER TABLE external_authentications ADD CONSTRAINT external_authentications_auth_type_check 
    CHECK (auth_type IN ('BASIC', 'OAUTH1', 'OAUTH2', 'API_KEY', 'CUSTOM', 'HMAC', 'CERTIFICATE', 'CUSTOM_HEADER'));