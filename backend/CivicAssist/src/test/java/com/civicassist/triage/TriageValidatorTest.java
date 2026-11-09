package com.civicassist.triage;

import com.civicassist.triage.dto.ExtractedDetail;
import com.civicassist.triage.dto.TriageSuggestion;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TriageValidatorTest {

    private final TriageValidator validator=new TriageValidator();

    private final Set<String> allowedDepartments=Set.of(
            "Roads and Infrastructure Department",
            "Water and pipelines",
            "Waste Management Department"
    );

    private TriageSuggestion validSuggestion() {
        return new TriageSuggestion(
                "ROADS",
                "HIGH",
                "Roads and Infrastructure Department",
                "The reported pothole may create a road safety risk",
                "A pothole has been reported that may create a road safety risk.",
                List.of(
                        new ExtractedDetail("issue", "Pothole"),
                        new ExtractedDetail("impact", "Road safety risk")
                ),
                List.of("Exact pothole location")
        );
    }

    private void assertInvalid(TriageSuggestion suggestion){
        ResponseStatusException exception=assertThrows(ResponseStatusException.class,
                ()->validator.validate(suggestion, allowedDepartments));
        assertEquals(HttpStatus.BAD_GATEWAY,exception.getStatusCode());
    }

    @Test
    void shouldAcceptValidSuggestion(){
        TriageSuggestion result=validator.validate(
                validSuggestion(),
                allowedDepartments
        );
        assertNotNull(result);
        assertEquals("ROADS",result.category());
        assertEquals("HIGH",result.urgency());
        assertEquals("Roads and Infrastructure Department",result.department());

    }

    @Test
    void shouldNormalizeCategoryAndUrgency(){
        TriageSuggestion suggestion=new TriageSuggestion(
                "roads",
                "high",
                "Roads and Infrastructure Department",
                "The reported pothole may create a road safety risk",
                "A pothole has been reported that may create a road safety risk.",
                List.of(
                        new ExtractedDetail("issue", "Pothole"),
                        new ExtractedDetail("impact", "Road safety risk")
                ),
                List.of("Exact pothole location")
        );
        TriageSuggestion result=validator.validate(suggestion,allowedDepartments);
        assertEquals("ROADS",result.category());
        assertEquals("HIGH",result.urgency());
    }

    @Test
    void shouldTrimDepartmentAndReasoning() {
        TriageSuggestion suggestion = new TriageSuggestion(
                "ROADS",
                "HIGH",
                " Roads and Infrastructure Department  ",
                "  Pothole reported on the road.  ",
                "A pothole has been reported that may create a road safety risk.",
                List.of(
                        new ExtractedDetail("issue", "Pothole"),
                        new ExtractedDetail("impact", "Road safety risk")
                ),
                List.of("Exact pothole location")
        );

        TriageSuggestion result = validator.validate(
                suggestion,
                allowedDepartments
        );

        assertEquals("Roads and Infrastructure Department", result.department());
        assertEquals("Pothole reported on the road.", result.reasoning());
    }

    @Test
    void shouldRejectNullSuggestion() {
        assertInvalid(null);
    }

    @Test
    void shouldRejectNullDepartmentSet() {
        assertThrows(
                ResponseStatusException.class,
                () -> validator.validate(validSuggestion(), null)
        );
    }

    @Test
    void shouldRejectEmptyDepartmentSet() {
        assertThrows(
                ResponseStatusException.class,
                () -> validator.validate(validSuggestion(), Set.of())
        );
    }

    @Test
    void shouldRejectInvalidCategory() {
        TriageSuggestion suggestion = new TriageSuggestion(
                "ELECTRICITY",
                "HIGH",
                "Roads and Infrastructure Department",
                "Electrical issue reported.",
                "A pothole has been reported that may create a road safety risk.",
                List.of(
                        new ExtractedDetail("issue", "Pothole"),
                        new ExtractedDetail("impact", "Road safety risk")
                ),
                List.of("Exact pothole location")
        );

        assertInvalid(suggestion);
    }

    @Test
    void shouldRejectNullCategory() {
        TriageSuggestion suggestion = new TriageSuggestion(
                null,
                "HIGH",
                "Roads and Infrastructure Department",
                "Pothole reported.",
                "A pothole has been reported that may create a road safety risk.",
                List.of(
                        new ExtractedDetail("issue", "Pothole"),
                        new ExtractedDetail("impact", "Road safety risk")
                ),
                List.of("Exact pothole location")
        );

        assertInvalid(suggestion);
    }

    @Test
    void shouldRejectInvalidUrgency() {
        TriageSuggestion suggestion = new TriageSuggestion(
                "ROADS",
                "URGENT",
                "Roads and Infrastructure Department",
                "Pothole reported.",
                "A pothole has been reported that may create a road safety risk.",
                List.of(
                        new ExtractedDetail("issue", "Pothole"),
                        new ExtractedDetail("impact", "Road safety risk")
                ),
                List.of("Exact pothole location")
        );

        assertInvalid(suggestion);
    }

    @Test
    void shouldRejectNullUrgency() {
        TriageSuggestion suggestion = new TriageSuggestion(
                "ROADS",
                null,
                "Roads and Infrastructure Department",
                "Pothole reported.",
                "A pothole has been reported that may create a road safety risk.",
                List.of(
                        new ExtractedDetail("issue", "Pothole"),
                        new ExtractedDetail("impact", "Road safety risk")
                ),
                List.of("Exact pothole location")
        );

        assertInvalid(suggestion);
    }

    @Test
    void shouldRejectDepartmentNotInAllowedList() {
        TriageSuggestion suggestion = new TriageSuggestion(
                "ROADS",
                "HIGH",
                "Electrical Department",
                "Pothole reported.",
                "A pothole has been reported that may create a road safety risk.",
                List.of(
                        new ExtractedDetail("issue", "Pothole"),
                        new ExtractedDetail("impact", "Road safety risk")
                ),
                List.of("Exact pothole location")
        );

        assertInvalid(suggestion);
    }

    @Test
    void shouldRejectDepartmentWithIncorrectCase() {
        TriageSuggestion suggestion = new TriageSuggestion(
                "ROADS",
                "HIGH",
                "roads and infrastructure department",
                "Pothole reported.",
                "A pothole has been reported that may create a road safety risk.",
                List.of(
                        new ExtractedDetail("issue", "Pothole"),
                        new ExtractedDetail("impact", "Road safety risk")
                ),
                List.of("Exact pothole location")
        );

        assertInvalid(suggestion);
    }

    @Test
    void shouldRejectNullDepartment() {
        TriageSuggestion suggestion = new TriageSuggestion(
                "ROADS",
                "HIGH",
                null,
                "Pothole reported.",
                "A pothole has been reported that may create a road safety risk.",
                List.of(
                        new ExtractedDetail("issue", "Pothole"),
                        new ExtractedDetail("impact", "Road safety risk")
                ),
                List.of("Exact pothole location")
        );

        assertInvalid(suggestion);
    }

    @Test
    void shouldRejectNullReasoning() {
        TriageSuggestion suggestion = new TriageSuggestion(
                "ROADS",
                "HIGH",
                "Roads and Infrastructure Department",
                null,
                "A pothole has been reported that may create a road safety risk.",
                List.of(
                        new ExtractedDetail("issue", "Pothole"),
                        new ExtractedDetail("impact", "Road safety risk")
                ),
                List.of("Exact pothole location")
        );

        assertInvalid(suggestion);
    }

    @Test
    void shouldRejectBlankReasoning() {
        TriageSuggestion suggestion = new TriageSuggestion(
                "ROADS",
                "HIGH",
                "Roads and Infrastructure",
                "   ",
                "A pothole has been reported that may create a road safety risk.",
                List.of(
                        new ExtractedDetail("issue", "Pothole"),
                        new ExtractedDetail("impact", "Road safety risk")
                ),
                List.of("Exact pothole location")
        );

        assertInvalid(suggestion);
    }

    @Test
    void shouldAcceptReasoningOfExactly1000Characters() {
        String reasoning = "A".repeat(1000);

        TriageSuggestion suggestion = new TriageSuggestion(
                "ROADS",
                "HIGH",
                "Roads and Infrastructure Department",
                reasoning,
                "A pothole has been reported that may create a road safety risk.",
                List.of(
                        new ExtractedDetail("issue", "Pothole"),
                        new ExtractedDetail("impact", "Road safety risk")
                ),
                List.of("Exact pothole location")
        );

        TriageSuggestion result = validator.validate(
                suggestion,
                allowedDepartments
        );

        assertEquals(1000, result.reasoning().length());
    }

    @Test
    void shouldRejectReasoningLongerThan1000Characters() {
        String reasoning = "A".repeat(1001);

        TriageSuggestion suggestion = new TriageSuggestion(
                "ROADS",
                "HIGH",
                "Roads and Infrastructure Department",
                reasoning,
                "A pothole has been reported that may create a road safety risk.",
                List.of(
                        new ExtractedDetail("issue", "Pothole"),
                        new ExtractedDetail("impact", "Road safety risk")
                ),
                List.of("Exact pothole location")
        );

        assertInvalid(suggestion);
    }


    @Test
    void shouldAcceptValidExtraction() {
        TriageSuggestion result = validator.validate(
                validSuggestion(),
                allowedDepartments
        );

        assertEquals(
                "A pothole has been reported that may create a road safety risk.",
                result.summary()
        );

        assertEquals(2, result.extractedDetails().size());
        assertEquals("issue", result.extractedDetails().get(0).field());
        assertEquals("Pothole", result.extractedDetails().get(0).value());

        assertEquals(
                List.of("Exact pothole location"),
                result.missingInformation()
        );
    }

    @Test
    void shouldRejectNullSummary() {
        TriageSuggestion suggestion = new TriageSuggestion(
                "ROADS", "HIGH",
                "Roads and Infrastructure Department",
                "Valid reasoning",
                null,
                List.of(),
                List.of()
        );

        assertInvalid(suggestion);
    }

    @Test
    void shouldRejectBlankSummary() {
        TriageSuggestion suggestion = new TriageSuggestion(
                "ROADS", "HIGH",
                "Roads and Infrastructure Department",
                "Valid reasoning",
                "   ",
                List.of(),
                List.of()
        );

        assertInvalid(suggestion);
    }

    @Test
    void shouldRejectSummaryLongerThan500Characters() {
        TriageSuggestion suggestion = new TriageSuggestion(
                "ROADS", "HIGH",
                "Roads and Infrastructure Department",
                "Valid reasoning",
                "A".repeat(501),
                List.of(),
                List.of()
        );

        assertInvalid(suggestion);
    }

    @Test
    void shouldRejectNullExtractedDetails() {
        TriageSuggestion suggestion = new TriageSuggestion(
                "ROADS", "HIGH",
                "Roads and Infrastructure Department",
                "Valid reasoning",
                "Valid summary",
                null,
                List.of()
        );

        assertInvalid(suggestion);
    }

    @Test
    void shouldRejectBlankExtractedField() {
        TriageSuggestion suggestion = new TriageSuggestion(
                "ROADS", "HIGH",
                "Roads and Infrastructure Department",
                "Valid reasoning",
                "Valid summary",
                List.of(new ExtractedDetail(" ", "Pothole")),
                List.of()
        );

        assertInvalid(suggestion);
    }

    @Test
    void shouldRejectBlankExtractedValue() {
        TriageSuggestion suggestion = new TriageSuggestion(
                "ROADS", "HIGH",
                "Roads and Infrastructure Department",
                "Valid reasoning",
                "Valid summary",
                List.of(new ExtractedDetail("issue", " ")),
                List.of()
        );

        assertInvalid(suggestion);
    }

    @Test
    void shouldRejectMoreThan10ExtractedDetails() {
        List<ExtractedDetail> details = java.util.stream.IntStream
                .range(0, 11)
                .mapToObj(i -> new ExtractedDetail("field" + i, "value" + i))
                .toList();

        TriageSuggestion suggestion = new TriageSuggestion(
                "ROADS", "HIGH",
                "Roads and Infrastructure Department",
                "Valid reasoning",
                "Valid summary",
                details,
                List.of()
        );

        assertInvalid(suggestion);
    }

    @Test
    void shouldRejectNullMissingInformation() {
        TriageSuggestion suggestion = new TriageSuggestion(
                "ROADS", "HIGH",
                "Roads and Infrastructure Department",
                "Valid reasoning",
                "Valid summary",
                List.of(),
                null
        );

        assertInvalid(suggestion);
    }

    @Test
    void shouldRejectBlankMissingInformation() {
        TriageSuggestion suggestion = new TriageSuggestion(
                "ROADS", "HIGH",
                "Roads and Infrastructure Department",
                "Valid reasoning",
                "Valid summary",
                List.of(),
                List.of(" ")
        );

        assertInvalid(suggestion);
    }

    @Test
    void shouldRejectMoreThan10MissingInformationEntries() {
        List<String> missing = java.util.stream.IntStream
                .range(0, 11)
                .mapToObj(i -> "Missing field " + i)
                .toList();

        TriageSuggestion suggestion = new TriageSuggestion(
                "ROADS", "HIGH",
                "Roads and Infrastructure Department",
                "Valid reasoning",
                "Valid summary",
                List.of(),
                missing
        );

        assertInvalid(suggestion);
    }

    @Test
    void shouldAllowEmptyExtractionLists() {
        TriageSuggestion suggestion = new TriageSuggestion(
                "ROADS", "HIGH",
                "Roads and Infrastructure Department",
                "Valid reasoning",
                "Valid summary",
                List.of(),
                List.of()
        );

        TriageSuggestion result = validator.validate(
                suggestion,
                allowedDepartments
        );

        assertNotNull(result);
        assertTrue(result.extractedDetails().isEmpty());
        assertTrue(result.missingInformation().isEmpty());
    }

    @Test
    void shouldTrimExtractionFields() {
        TriageSuggestion suggestion = new TriageSuggestion(
                "ROADS",
                "HIGH",
                "Roads and Infrastructure Department",
                " Valid reasoning ",
                " Valid summary ",
                List.of(new ExtractedDetail(" issue ", " Pothole ")),
                List.of(" Exact location ")
        );

        TriageSuggestion result = validator.validate(
                suggestion,
                allowedDepartments
        );

        assertEquals("Valid summary", result.summary());
        assertEquals("issue", result.extractedDetails().get(0).field());
        assertEquals("Pothole", result.extractedDetails().get(0).value());
        assertEquals("Exact location", result.missingInformation().get(0));
    }


}
