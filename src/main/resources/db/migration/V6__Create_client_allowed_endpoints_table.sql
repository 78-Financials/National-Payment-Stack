-- Create client_allowed_endpoints table for storing client endpoint permissions
-- Note: Foreign key constraint will be added later to avoid migration issues
CREATE TABLE IF NOT EXISTS client_allowed_endpoints (
    client_id BIGINT NOT NULL,
    endpoint VARCHAR(255) NOT NULL,
    PRIMARY KEY (client_id, endpoint)
);
