-- Add missing description column to internal_clients table
ALTER TABLE internal_clients 
ADD COLUMN IF NOT EXISTS description VARCHAR(500);
