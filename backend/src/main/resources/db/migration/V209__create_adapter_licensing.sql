-- Adapter Licensing and Subscription Management

-- Adapter licensing requirements
CREATE TABLE adapter_licensing (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    adapter_type_id UUID REFERENCES adapter_types(id) ON DELETE CASCADE,
    
    -- Licensing model
    licensing_model VARCHAR(50) NOT NULL CHECK (licensing_model IN (
        'free',           -- No license required
        'byol',           -- Bring Your Own License
        'integrix',       -- Licensed through Integrix
        'marketplace',    -- Available in Integrix marketplace
        'contact'         -- Contact sales
    )),
    
    -- License requirements
    requires_vendor_account BOOLEAN DEFAULT false,
    requires_api_key BOOLEAN DEFAULT false,
    requires_license_key BOOLEAN DEFAULT false,
    
    -- Vendor information
    vendor_signup_url TEXT,
    vendor_pricing_url TEXT,
    vendor_contact_email TEXT,
    
    -- Integrix marketplace (if applicable)
    marketplace_enabled BOOLEAN DEFAULT false,
    base_price DECIMAL(10,2),
    price_unit VARCHAR(50), -- 'per_month', 'per_year', 'per_message', 'per_gb'
    
    -- Usage limits
    free_tier_limit INTEGER, -- e.g., 1000 messages/month
    free_tier_unit VARCHAR(50),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Customer adapter subscriptions
CREATE TABLE adapter_subscriptions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID NOT NULL, -- Reference to customer organization
    adapter_type_id UUID REFERENCES adapter_types(id),
    
    -- Subscription details
    subscription_type VARCHAR(50) CHECK (subscription_type IN (
        'free',
        'trial',
        'paid',
        'enterprise'
    )),
    status VARCHAR(50) DEFAULT 'active',
    
    -- License information
    license_key TEXT, -- Encrypted
    api_key TEXT, -- Encrypted
    vendor_account_id TEXT,
    
    -- Validity
    valid_from DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_until DATE,
    
    -- Usage tracking
    usage_limit BIGINT,
    usage_unit VARCHAR(50),
    current_usage BIGINT DEFAULT 0,
    last_reset_date DATE DEFAULT CURRENT_DATE,
    
    -- Billing
    billing_cycle VARCHAR(20), -- 'monthly', 'yearly'
    next_billing_date DATE,
    amount DECIMAL(10,2),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Usage tracking for billing
CREATE TABLE adapter_usage_tracking (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    adapter_instance_id UUID REFERENCES adapter_instances(id),
    subscription_id UUID REFERENCES adapter_subscriptions(id),
    
    -- Usage metrics
    usage_date DATE NOT NULL,
    message_count BIGINT DEFAULT 0,
    data_volume_bytes BIGINT DEFAULT 0,
    api_calls BIGINT DEFAULT 0,
    error_count BIGINT DEFAULT 0,
    
    -- Performance metrics
    avg_response_time_ms INTEGER,
    max_response_time_ms INTEGER,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Composite unique constraint
    UNIQUE(adapter_instance_id, usage_date)
);

-- Marketplace offerings
CREATE TABLE adapter_marketplace_offerings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    adapter_type_id UUID REFERENCES adapter_types(id),
    
    -- Pricing tiers
    tier_name VARCHAR(100) NOT NULL,
    tier_description TEXT,
    
    -- Pricing
    monthly_price DECIMAL(10,2),
    yearly_price DECIMAL(10,2),
    
    -- Included usage
    included_messages BIGINT,
    included_data_gb INTEGER,
    included_api_calls BIGINT,
    
    -- Overage pricing
    overage_price_per_1k_messages DECIMAL(10,2),
    overage_price_per_gb DECIMAL(10,2),
    
    -- Features
    features JSONB, -- List of included features
    
    display_order INTEGER DEFAULT 0,
    is_popular BOOLEAN DEFAULT false,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_adapter_licensing_model ON adapter_licensing(licensing_model);
CREATE INDEX idx_adapter_subscriptions_org ON adapter_subscriptions(organization_id);
CREATE INDEX idx_adapter_subscriptions_status ON adapter_subscriptions(status);
CREATE INDEX idx_usage_tracking_date ON adapter_usage_tracking(usage_date);
CREATE INDEX idx_usage_tracking_subscription ON adapter_usage_tracking(subscription_id);

-- Insert licensing info for core adapters
INSERT INTO adapter_licensing (adapter_type_id, licensing_model, requires_vendor_account)
SELECT id, 'free', false FROM adapter_types WHERE vendor = 'Integrix';

-- Insert licensing info for third-party adapters (examples)
INSERT INTO adapter_licensing (adapter_type_id, licensing_model, requires_vendor_account, requires_api_key, vendor_signup_url, vendor_pricing_url)
SELECT 
    id, 
    'byol', 
    true, 
    true, 
    'https://developer.salesforce.com/signup',
    'https://www.salesforce.com/pricing/'
FROM adapter_types WHERE code = 'salesforce';

INSERT INTO adapter_licensing (adapter_type_id, licensing_model, requires_vendor_account, requires_license_key, vendor_contact_email)
SELECT 
    id, 
    'contact', 
    true, 
    true, 
    'sales@sap.com'
FROM adapter_types WHERE code = 'sap-s4hana';