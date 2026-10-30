ALTER TABLE users
    ADD COLUMN department_id BIGINT;

ALTER TABLE users
    ADD CONSTRAINT fk_users_department
        FOREIGN KEY (department_id)
            REFERENCES departments(id)
            ON DELETE RESTRICT;

CREATE INDEX idx_users_department_id
    ON users(department_id);