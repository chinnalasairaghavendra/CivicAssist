package com.civicassist.triage;

import com.civicassist.triage.dto.TriageSuggestion;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TriageValidatorTest {

    private final TriageValidator validator=new TriageValidator();

    private final Set<String> allowedDepartments=Set.of(
            "Roads and Infrastructure Department",
            "Water and pipelines",
            "Waste Management Department"
    );

    private TriageSuggestion validSuggestion(){
        return new TriageSuggestion(
                "ROADS",
                "HIGH",
                "Roads and Infrastructure Department",
                "The reported pothole may create a road safety risk"
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
                "Pothole reported on the road."
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
                "  Pothole reported on the road.  "
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
                "Electrical issue reported."
        );

        assertInvalid(suggestion);
    }

    @Test
    void shouldRejectNullCategory() {
        TriageSuggestion suggestion = new TriageSuggestion(
                null,
                "HIGH",
                "Roads and Infrastructure Department",
                "Pothole reported."
        );

        assertInvalid(suggestion);
    }

    @Test
    void shouldRejectInvalidUrgency() {
        TriageSuggestion suggestion = new TriageSuggestion(
                "ROADS",
                "URGENT",
                "Roads and Infrastructure Department",
                "Pothole reported."
        );

        assertInvalid(suggestion);
    }

    @Test
    void shouldRejectNullUrgency() {
        TriageSuggestion suggestion = new TriageSuggestion(
                "ROADS",
                null,
                "Roads and Infrastructure Department",
                "Pothole reported."
        );

        assertInvalid(suggestion);
    }

    @Test
    void shouldRejectDepartmentNotInAllowedList() {
        TriageSuggestion suggestion = new TriageSuggestion(
                "ROADS",
                "HIGH",
                "Electrical Department",
                "Pothole reported."
        );

        assertInvalid(suggestion);
    }

    @Test
    void shouldRejectDepartmentWithIncorrectCase() {
        TriageSuggestion suggestion = new TriageSuggestion(
                "ROADS",
                "HIGH",
                "roads and infrastructure department",
                "Pothole reported."
        );

        assertInvalid(suggestion);
    }

    @Test
    void shouldRejectNullDepartment() {
        TriageSuggestion suggestion = new TriageSuggestion(
                "ROADS",
                "HIGH",
                null,
                "Pothole reported."
        );

        assertInvalid(suggestion);
    }

    @Test
    void shouldRejectNullReasoning() {
        TriageSuggestion suggestion = new TriageSuggestion(
                "ROADS",
                "HIGH",
                "Roads and Infrastructure Department",
                null
        );

        assertInvalid(suggestion);
    }

    @Test
    void shouldRejectBlankReasoning() {
        TriageSuggestion suggestion = new TriageSuggestion(
                "ROADS",
                "HIGH",
                "Roads and Infrastructure",
                "   "
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
                reasoning
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
                reasoning
        );

        assertInvalid(suggestion);
    }



}
