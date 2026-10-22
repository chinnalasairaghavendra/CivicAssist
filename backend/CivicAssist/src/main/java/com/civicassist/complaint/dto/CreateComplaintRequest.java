package com.civicassist.complaint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateComplaintRequest(
        @NotBlank
        @Size(max = 160)
        String title,

        @NotBlank
        String description,

        @Size(max = 80)
        String category,

        @Size(max = 255)
        String address,

        Double latitude,

        Double longitude
) {




}
