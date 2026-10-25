package com.civicassist.triage.dto;

public record TriageSuggestion(
        String category,
        String urgency,
        String department,
        String reasoning
) {


}
