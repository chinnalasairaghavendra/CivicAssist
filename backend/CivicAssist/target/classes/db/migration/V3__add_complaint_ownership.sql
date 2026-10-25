
ALTER TABLE complaints
    ADD COLUMN user_id BIGINT;

ALTER TABLE complaints
    ADD CONSTRAINT fk_complaints_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE SET NULL;

CREATE INDEX idx_complaints_user_id
    ON complaints(user_id);
