package com.qualitysports.backend.auth.dto;

public record AuthResponse(
        String token,
        String email,
        String nombre,
        String role
) {
}
