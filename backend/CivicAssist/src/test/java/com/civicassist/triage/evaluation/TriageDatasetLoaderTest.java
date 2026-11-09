package com.civicassist.triage.evaluation;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class TriageDatasetLoaderTest {

    private final TriageDatasetLoader loader =
            new TriageDatasetLoader();

    private static final String DATASET_PATH =
            "ai-evaluation/triage-dataset.csv";

    @Test
    void shouldLoadDatasetSuccessfully() {

        List<TriageDatasetRow> rows =
                loader.load(DATASET_PATH);

        assertNotNull(rows);
        assertFalse(rows.isEmpty());

        assertEquals(60, rows.size());
    }

    @Test
    void shouldContainUniqueIds() {

        List<TriageDatasetRow> rows =
                loader.load(DATASET_PATH);

        Set<Long> ids = rows.stream()
                .map(TriageDatasetRow::id)
                .collect(Collectors.toSet());

        assertEquals(rows.size(), ids.size());
    }

    @Test
    void shouldContainValidCategoryAndUrgencyValues() {

        List<TriageDatasetRow> rows =
                loader.load(DATASET_PATH);

        Set<String> categories = Set.of(
                "ROADS",
                "WATER_SUPPLY",
                "SANITATION",
                "DRAINAGE",
                "STREETLIGHTS",
                "WASTE_MANAGEMENT",
                "PUBLIC_SAFETY",
                "OTHER"
        );

        Set<String> urgencies = Set.of(
                "LOW",
                "MEDIUM",
                "HIGH",
                "CRITICAL"
        );

        for (TriageDatasetRow row : rows) {

            assertTrue(
                    categories.contains(row.expectedCategory()),
                    "Invalid category in dataset ID: " + row.id()
            );

            assertTrue(
                    urgencies.contains(row.expectedUrgency()),
                    "Invalid urgency in dataset ID: " + row.id()
            );
        }
    }

    @Test
    void shouldRejectMissingDataset() {

        assertThrows(
                IllegalArgumentException.class,
                () -> loader.load("missing-dataset.csv")
        );
    }
}
