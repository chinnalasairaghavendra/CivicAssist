package com.civicassist.assignment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AssignDepartmentRequest(
        @NotBlank(message="Department is required")
        @Size(max=100,message = "Department must not exceed 100 characters")
        String department,
        @Size(max=100,message="Assignment must not exceed 1000 characters")
        String assignmentNote
) {
}
