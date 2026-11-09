package com.civicassist.triage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ExtractedDetail(

        @NotBlank
        @Size(max = 100)
        String field,

        @NotBlank
        @Size(max = 500)
        String value

) {
}