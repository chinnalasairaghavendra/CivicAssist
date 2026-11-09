package com.civicassist.triage.evaluation;

public record TriageDatasetRow(
        Long id,
        String title,
        String description,
        String expectedCategory,
        String expectedUrgency,
        String expectedDepartment
) {
}
