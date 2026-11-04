package com.civicassist.triage;

import com.civicassist.triage.dto.TriageSuggestion;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Set;

@Component
public class TriageValidator {

    private static final Set<String> ALLOWED_CATEGORIES=Set.of(
            "ROADS",
            "WATER_SUPPLY",
            "SANITATION",
            "DRAINAGE",
            "STREETLIGHTS",
            "WASTE_MANAGEMENT",
            "PUBLIC_SAFETY",
            "OTHER"
    );

    private static final Set<String> ALLOWED_URGENCIES=Set.of(
            "LOW",
            "MEDIUM",
            "HIGH",
            "CRITICAL"
    );

    public TriageSuggestion validate(
            TriageSuggestion suggestion,
            Set<String> allowedDepartmentNames
    ){
        if(suggestion==null)
        {
            throw invalidResponse();
        }
        String category=normalize(suggestion.category());
        String urgency=normalize(suggestion.urgency());

        String department = suggestion.department()==null ? "":suggestion.department().trim();
        String reasoning = suggestion.reasoning()==null ? "": suggestion.reasoning().trim();

        if(!ALLOWED_CATEGORIES.contains(category)||
                !ALLOWED_URGENCIES.contains(urgency)||
                allowedDepartmentNames == null ||
                allowedDepartmentNames.isEmpty() ||
                !allowedDepartmentNames.contains(department) ||
                reasoning.isBlank()||reasoning.length()>1000
        ){
            throw invalidResponse();
        }

        return new TriageSuggestion(
                category,
                urgency,
                department,
                reasoning
        );
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value.trim().toUpperCase(Locale.ROOT);
    }

    private ResponseStatusException invalidResponse() {
        return new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "AI triage returned an invalid classification"
        );
    }

}
