package com.civicassist.department.dto;

import com.civicassist.department.Department;

import java.time.OffsetDateTime;

public record DepartmentResponse(
        Long id,
        String name,
        String code,
        String description,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static DepartmentResponse from(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getCode(),
                department.getDescription(),
                department.isActive(),
                department.getCreatedAt(),
                department.getUpdatedAt()
        );
    }
}