package com.civicassist.triage;

import com.civicassist.triage.dto.ExtractedDetail;
import com.civicassist.triage.dto.TriageSuggestion;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
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
        String summary=suggestion.summary()==null?"":suggestion.summary().trim();

        if(!ALLOWED_CATEGORIES.contains(category)||
                !ALLOWED_URGENCIES.contains(urgency)||
                allowedDepartmentNames == null ||
                allowedDepartmentNames.isEmpty() ||
                !allowedDepartmentNames.contains(department) ||
                reasoning.isBlank()||reasoning.length()>1000 ||
                summary.isBlank() ||
                summary.length() > 500
                || !isValidExtractedDetails(suggestion.extractedDetails())
                || !isValidMissingInformation(suggestion.missingInformation())
        ){
            throw invalidResponse();
        }

        List<ExtractedDetail> extractedDetails=suggestion.extractedDetails().stream()
                .map(detail->new ExtractedDetail(
                        detail.field().trim(),
                        detail.value().trim()
                )).toList();

        List<String> missingInformation =
                suggestion.missingInformation().stream()
                        .map(String::trim)
                        .toList();
        return new TriageSuggestion(
                category,
                urgency,
                department,
                reasoning,
                summary,
                extractedDetails,
                missingInformation
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

    private boolean isValidExtractedDetails(
            List<ExtractedDetail> details
    ) {

        if (details == null || details.size() > 10) {
            return false;
        }

        return details.stream().allMatch(detail ->
                detail != null
                        && detail.field() != null
                        && !detail.field().isBlank()
                        && detail.field().trim().length() <= 100
                        && detail.value() != null
                        && !detail.value().isBlank()
                        && detail.value().trim().length() <= 500
        );
    }

    private boolean isValidMissingInformation(
            List<String> missingInformation
    ) {

        if (missingInformation == null
                || missingInformation.size() > 10) {
            return false;
        }

        return missingInformation.stream().allMatch(item ->
                item != null
                        && !item.isBlank()
                        && item.trim().length() <= 200
        );
    }

}
