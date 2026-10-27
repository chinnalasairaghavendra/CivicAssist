package com.civicassist.assignment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AssignDepartmentRequest(
        @NotBlank(message = "Department code is required")
        @Size(max = 30, message = "Department code must not exceed 30 characters")
        @Pattern(
                regexp = "^[A-Za-z][A-Za-z0-9_-]*$",
                message = "Invalid department code format"
        )
        String departmentCode,
        @Size(max=1000,message="Assignment must not exceed 1000 characters")
        String assignmentNote
) {
}
