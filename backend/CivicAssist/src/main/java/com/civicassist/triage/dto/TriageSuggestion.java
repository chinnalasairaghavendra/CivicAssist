package com.civicassist.triage.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TriageSuggestion(
        @NotBlank
        @Size(max=50)
        String category,

        @NotBlank
        @Size(max=20)
        String urgency,

        @NotBlank
        @Size(max=150)
        String department,

        @NotBlank
        @Size(max=1000)
        String reasoning
) {


}
