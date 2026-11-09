ALTER TABLE complaints
    ADD COLUMN ai_summary TEXT,
    ADD COLUMN ai_extracted_details JSONB,
    ADD COLUMN ai_missing_information JSONB;