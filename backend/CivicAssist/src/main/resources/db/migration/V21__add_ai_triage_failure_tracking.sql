ALTER TABLE complaints
    ADD COLUMN ai_triage_status VARCHAR(20) DEFAULT 'PENDING',
    ADD COLUMN ai_triage_attempts INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN ai_triage_error TEXT,
    ADD COLUMN ai_last_attempt_at TIMESTAMPTZ;