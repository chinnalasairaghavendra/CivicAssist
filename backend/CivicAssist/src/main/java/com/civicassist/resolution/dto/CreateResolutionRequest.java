package com.civicassist.resolution.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateResolutionRequest(
        @NotBlank(message="Resolution notes are required")
        @Size(max=10000,message="Resolution notes must not exceed 10000 characters")
        String resolutionNotes
) {
}
