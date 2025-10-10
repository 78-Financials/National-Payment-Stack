-- Add all missing columns to internal_clients table
-- This migration adds columns that exist in the entity but were missing from the original table creation

-- Add last_accessed_at column
ALTER TABLE internal_clients 
ADD COLUMN IF NOT EXISTS last_accessed_at TIMESTAMP;

-- Add updated_by column
ALTER TABLE internal_clients 
ADD COLUMN IF NOT EXISTS updated_by VARCHAR(50) DEFAULT 'SYSTEM';

-- Add last_activity column
ALTER TABLE internal_clients 
ADD COLUMN IF NOT EXISTS last_activity TIMESTAMP;

-- Add password_reset_token column
ALTER TABLE internal_clients 
ADD COLUMN IF NOT EXISTS password_reset_token VARCHAR(100);

-- Add password_reset_expires column
ALTER TABLE internal_clients 
ADD COLUMN IF NOT EXISTS password_reset_expires TIMESTAMP;
