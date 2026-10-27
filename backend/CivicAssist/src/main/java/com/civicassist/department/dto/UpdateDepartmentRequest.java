package com.civicassist.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateDepartmentRequest(

        @NotBlank(message = "Department name is required")
        @Size(max = 100, message = "Department name cannot exceed 100 characters")
        String name,

        @NotBlank(message = "Department code is required")
        @Size(max = 30, message = "Department code cannot exceed 30 characters")
        @Pattern(
                regexp = "^[A-Za-z][A-Za-z0-9_-]*$",
                message = "Department code must start with a letter and contain only letters, digits, underscores, or hyphens"
        )
        String code,

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        String description
) {
}