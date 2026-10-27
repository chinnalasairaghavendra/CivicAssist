-- 1. Add the new foreign-key column.
ALTER TABLE complaint_assignments
    ADD COLUMN department_id BIGINT;

-- 2. Map existing assignment records to department IDs.
UPDATE complaint_assignments ca
SET department_id = d.id
    FROM departments d
WHERE
    (
    LOWER(TRIM(ca.department)) =
    LOWER(TRIM(d.name))
    )
   OR
    (
    LOWER(TRIM(ca.department)) = 'roads and infrastructure'
  AND d.code = 'ROADS'
    );

-- 3. Prevent migration if any assignments remain unmapped.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM complaint_assignments
        WHERE department_id IS NULL
    ) THEN
        RAISE EXCEPTION
            'Migration aborted: some complaint assignments could not be mapped to departments';
END IF;
END $$;

-- 4. Enforce the relationship.
ALTER TABLE complaint_assignments
    ALTER COLUMN department_id SET NOT NULL;

ALTER TABLE complaint_assignments
    ADD CONSTRAINT fk_complaint_assignments_department
        FOREIGN KEY (department_id)
            REFERENCES departments(id)
            ON DELETE RESTRICT;

-- 5. Add an index for department-based queries.
CREATE INDEX idx_complaint_assignments_department_id
    ON complaint_assignments(department_id);

-- 6. Remove the old string-based department column.
ALTER TABLE complaint_assignments
DROP COLUMN department;