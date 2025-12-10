-- Create internal_clients table for storing client information
CREATE TABLE IF NOT EXISTS internal_clients (
    id BIGSERIAL PRIMARY KEY,
    client_id VARCHAR(50) NOT NULL UNIQUE,
    client_name VARCHAR(100) NOT NULL,
    api_key VARCHAR(255) NOT NULL UNIQUE,
    contact_email VARCHAR(100),
    contact_phone VARCHAR(20),
    webhook_url VARCHAR(500),
    rate_limit_per_minute INTEGER DEFAULT 100,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    client_type VARCHAR(20) DEFAULT 'BANK',
    transaction_prefix VARCHAR(10) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    last_login TIMESTAMP,
    login_attempts INTEGER DEFAULT 0,
    account_locked BOOLEAN DEFAULT false,
    description VARCHAR(500)
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_internal_clients_client_id ON internal_clients(client_id);
CREATE INDEX IF NOT EXISTS idx_internal_clients_api_key ON internal_clients(api_key);
CREATE INDEX IF NOT EXISTS idx_internal_clients_email ON internal_clients(email);
CREATE INDEX IF NOT EXISTS idx_internal_clients_active ON internal_clients(active);
CREATE INDEX IF NOT EXISTS idx_internal_clients_client_type ON internal_clients(client_type);
