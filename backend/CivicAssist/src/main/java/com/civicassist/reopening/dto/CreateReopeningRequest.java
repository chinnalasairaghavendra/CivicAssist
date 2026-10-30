package com.civicassist.reopening.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateReopeningRequest(
        @NotBlank(message = "A reopening reason is required")
        @Size(max = 1000, message = "Reason cannot exceed 1000 characters")
        String reason
) {}