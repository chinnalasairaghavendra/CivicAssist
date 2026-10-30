package com.civicassist.user.dto;

import com.civicassist.user.AppUser;

public record OfficialResponse(
        Long id,
        String name,
        String email,
        String role,
        Long departmentId,
        String departmentName
) {
    public static OfficialResponse from(AppUser user) {
        return new OfficialResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getDepartment().getId(),
                user.getDepartment().getName()
        );
    }
}