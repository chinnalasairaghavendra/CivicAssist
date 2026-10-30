package com.civicassist.reopening.dto;

import com.civicassist.reopening.ReopeningRequestStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewReopeningRequest(
        @NotNull(message = "A decision is required")
        ReopeningRequestStatus decision,

        @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
        String adminComment
) {
}