-- Seed initial adapter categories
INSERT INTO adapter_categories (code, name, description, icon, display_order) VALUES
    ('integrix', 'Integrix Flow Bridge', 'Core adapters built into Integrix Flow Bridge', 'layers', 0),
    ('crm', 'CRM & Sales', 'Customer Relationship Management and Sales platforms', 'users', 1),
    ('erp', 'ERP & Finance', 'Enterprise Resource Planning and Financial systems', 'building', 2),
    ('communication', 'Communication', 'Email, SMS, Chat, and Messaging platforms', 'message-circle', 3),
    ('ecommerce', 'E-Commerce', 'Online retail and marketplace platforms', 'shopping-cart', 4),
    ('database', 'Databases', 'SQL and NoSQL database systems', 'database', 5),
    ('storage', 'Storage', 'File storage and content management systems', 'hard-drive', 6),
    ('analytics', 'Analytics & BI', 'Business Intelligence and Analytics platforms', 'bar-chart', 7),
    ('marketing', 'Marketing', 'Marketing automation and campaign management', 'megaphone', 8),
    ('hr', 'HR & Workforce', 'Human Resources and workforce management', 'users', 9),
    ('iot', 'IoT & Devices', 'Internet of Things platforms and device management', 'cpu', 10),
    ('social', 'Social Media', 'Social media platforms and APIs', 'share-2', 11),
    ('payment', 'Payment Processing', 'Payment gateways and financial services', 'credit-card', 12),
    ('productivity', 'Productivity', 'Project management and productivity tools', 'check-square', 13),
    ('healthcare', 'Healthcare', 'Healthcare and medical systems', 'heart', 14),
    ('logistics', 'Logistics', 'Supply chain and logistics management', 'truck', 15),
    ('legacy', 'Legacy Systems', 'Traditional and legacy system adapters', 'archive', 16);

-- Add subcategories for some main categories
INSERT INTO adapter_categories (code, name, description, parent_category_id, display_order)
SELECT 'sap', 'SAP', 'SAP ecosystem adapters', id, 1 FROM adapter_categories WHERE code = 'erp';

INSERT INTO adapter_categories (code, name, description, parent_category_id, display_order)
SELECT 'microsoft', 'Microsoft', 'Microsoft platform adapters', id, 2 FROM adapter_categories WHERE code = 'erp';

INSERT INTO adapter_categories (code, name, description, parent_category_id, display_order)
SELECT 'oracle', 'Oracle', 'Oracle platform adapters', id, 3 FROM adapter_categories WHERE code = 'erp';