CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE consent_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    site_id UUID NOT NULL,
    visitor_id VARCHAR(255) NOT NULL,
    consent_given BOOLEAN NOT NULL,
    consent_scope VARCHAR(50) NOT NULL,
    ip_hash VARCHAR(128),
    recorded_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_consent_site_recorded ON consent_records (site_id, recorded_at DESC);
CREATE INDEX idx_consent_site_visitor ON consent_records (site_id, visitor_id);
CREATE INDEX idx_consent_scope ON consent_records (site_id, consent_scope);

CREATE TABLE deletion_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    site_id UUID NOT NULL,
    visitor_id VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    requested_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_deletion_site_status ON deletion_requests (site_id, status);
CREATE INDEX idx_deletion_status_requested ON deletion_requests (status, requested_at ASC);
CREATE INDEX idx_deletion_site_visitor ON deletion_requests (site_id, visitor_id);

CREATE TABLE daily_salts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    site_id UUID NOT NULL,
    salt_date DATE NOT NULL,
    salt_value VARCHAR(128) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (site_id, salt_date)
);

CREATE INDEX idx_daily_salt_lookup ON daily_salts (site_id, salt_date DESC);
