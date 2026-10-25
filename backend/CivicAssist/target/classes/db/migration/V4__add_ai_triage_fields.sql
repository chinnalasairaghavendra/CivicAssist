ALTER TABLE complaints
    ADD COLUMN ai_category VARCHAR(80),
    ADD COLUMN ai_urgency VARCHAR(20),
    ADD COLUMN recommended_department VARCHAR(100),
    ADD COLUMN ai_reasoning TEXT,
    ADD COLUMN triaged_at TIMESTAMP WITH TIME ZONE;