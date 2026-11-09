
package com.civicassist.triage.evaluation;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TriageDatasetLoader {

    private static final Set<String> REQUIRED_HEADERS = Set.of(
            "id",
            "title",
            "description",
            "expected_category",
            "expected_urgency",
            "expected_department"
    );

    public List<TriageDatasetRow> load(String resourcePath) {

        InputStream inputStream = getClass()
                .getClassLoader()
                .getResourceAsStream(resourcePath);

        if (inputStream == null) {
            throw new IllegalArgumentException(
                    "Dataset not found: " + resourcePath
            );
        }

        try (
                InputStreamReader reader =
                        new InputStreamReader(
                                inputStream,
                                StandardCharsets.UTF_8
                        );

                CSVParser parser = CSVFormat.DEFAULT.builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .setIgnoreEmptyLines(true)
                        .build()
                        .parse(reader)
        ) {

            validateHeaders(parser.getHeaderMap().keySet());

            List<TriageDatasetRow> rows = new ArrayList<>();
            Set<Long> ids = new HashSet<>();

            for (CSVRecord record : parser) {

                long rowNumber = record.getRecordNumber() + 1;

                Long id = parseId(record.get("id"), rowNumber);

                if (!ids.add(id)) {
                    throw new IllegalArgumentException(
                            "Duplicate dataset ID " + id
                                    + " at CSV row " + rowNumber
                    );
                }

                String title = requiredValue(
                        record.get("title"), "title", rowNumber
                );

                String description = requiredValue(
                        record.get("description"),
                        "description",
                        rowNumber
                );

                String category = requiredValue(
                        record.get("expected_category"),
                        "expected_category",
                        rowNumber
                ).toUpperCase();

                String urgency = requiredValue(
                        record.get("expected_urgency"),
                        "expected_urgency",
                        rowNumber
                ).toUpperCase();

                String department = requiredValue(
                        record.get("expected_department"),
                        "expected_department",
                        rowNumber
                );

                rows.add(new TriageDatasetRow(
                        id,
                        title,
                        description,
                        category,
                        urgency,
                        department
                ));
            }

            if (rows.isEmpty()) {
                throw new IllegalArgumentException(
                        "Dataset contains no complaint records"
                );
            }

            return List.copyOf(rows);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to read triage dataset",
                    e
            );
        }
    }

    private void validateHeaders(Set<String> actualHeaders) {

        if (!actualHeaders.containsAll(REQUIRED_HEADERS)) {

            Set<String> missingHeaders =
                    new HashSet<>(REQUIRED_HEADERS);

            missingHeaders.removeAll(actualHeaders);

            throw new IllegalArgumentException(
                    "Dataset is missing required headers: "
                            + missingHeaders
            );
        }
    }

    private Long parseId(String value, long rowNumber) {

        try {
            long id = Long.parseLong(value.trim());

            if (id <= 0) {
                throw new NumberFormatException();
            }

            return id;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid ID at CSV row " + rowNumber
                            + ": " + value
            );
        }
    }

    private String requiredValue(
            String value,
            String column,
            long rowNumber
    ) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Missing value for column '" + column
                            + "' at CSV row " + rowNumber
            );
        }

        return value.trim();
    }
}
