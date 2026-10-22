package com.civicassist.user.dto;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresIn,
        Long userId,
        String name,
        String email,
        String role
) {
}
